package com.dynamsoft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.cvr.CaptureVisionError;
import com.dynamsoft.cvr.CaptureVisionException;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.dynamsoft.dbr.DecodedBarcodesResult;
import com.dynamsoft.dcp.ParsedResult;
import com.dynamsoft.dcp.ParsedResultItem;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;

class GS1AIResult {

    static class GS1AIData {
        public String AI;
        public String Description;
        public String Value;
    }

    private final List<GS1AIData> datas;

    public GS1AIResult(ParsedResultItem item) {
        HashMap<String, GS1AIData> records = new HashMap<String, GS1AIData>();
        int count = item.getFieldCount();
        for (int i = 0; i < count; i++) {
            String name = item.getFieldName(i);
            String value = item.getFieldValue(name);

            String[] paths = name.split("\\.");
            if (paths.length == 2) {
                String ai = paths[0];
                if (!records.containsKey(ai)) {
                    GS1AIData data = new GS1AIData();
                    data.AI = ai;
                    records.put(ai, data);
                }

                if (paths[1].equals(ai + "AI")) {
                    records.get(ai).Description = value;
                } else if (paths[1].equals(ai + "Data")) {
                    records.get(ai).Value = value;
                }
            }
        }

        datas = new ArrayList<GS1AIData>();
        datas.addAll(records.values());

        datas.sort((a, b) -> a.AI.compareTo(b.AI));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GS1AIData item : datas) {
            sb.append("AI: ").append(item.AI).append(" (").append(item.Description).append("), Value: ").append(item.Value).append("\n");
        }

        return sb.toString();
    }
}

public class GS1AIScanner {

    public static void printResults(DecodedBarcodesResult barcodesResult, ParsedResult parsedResult) {
        BarcodeResultItem[] barcodeItems = barcodesResult.getItems();
        ParsedResultItem[] parsedItems = parsedResult.getItems();
        int length = Math.min(barcodeItems.length, parsedItems.length);
        for (int i = 0; i < length; i++) {
            System.out.println("Barcode result: " + barcodeItems[i].getText());
            GS1AIResult dlResult = new GS1AIResult(parsedItems[i]);
            String str = dlResult.toString();
            if (str.isEmpty()) {
                System.out.println("No Parsed GS1 Application Identifiers (AIs) detected.");
            } else {
                System.out.println("Parsed GS1 Application Identifiers (AIs):");
                System.out.println(str);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("**********************************************************");
        System.out.println("Welcome to Dynamsoft Capture Vision - GS1AIScanner Sample");
        System.out.println("**********************************************************");

        try {
            int errorCode = 0;
            String errorMsg = "";

            // Initialize license.
            // You can request and extend a trial license from https://www.dynamsoft.com/customer/license/trialLicense?product=dcv&utm_source=samples&package=java
            // The string 'DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9' here is a free public trial license. Note that network connection is required for this license to work.
            try {
                LicenseError licenseError = LicenseManager.initLicense("DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
                if (licenseError.getErrorCode() != EnumErrorCode.EC_OK) {
                    errorCode = licenseError.getErrorCode();
                    errorMsg = licenseError.getErrorString();
                }
            } catch (LicenseException e) {
                errorCode = e.getErrorCode();
                errorMsg = e.getErrorString();
            }

            if (errorCode != EnumErrorCode.EC_OK) {
                System.out.println("License initialization failed: ErrorCode: " + errorCode + ", ErrorString: " + errorMsg);
                System.out.print("Press Enter to quit...");
                scanner.nextLine();
                return;
            }

            CaptureVisionRouter cvRouter = new CaptureVisionRouter();
            while (true) {
                System.out.println("\n>> Input your image full path:");
                System.out.println("\n>> 'Enter' for sample image or 'Q'/'q' to quit");
                String imagePath = scanner.nextLine();

                if (imagePath.equalsIgnoreCase("q")) {
                    return;
                } else if (imagePath.isEmpty()) {
                    imagePath = "../../images/gs1-ai-sample.png";
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (!(new File(imagePath).exists())) {
                    System.out.println("The image does not exist.");
                    continue;
                }

                String templatePath = "../../CustomTemplates/ReadGS1AIBarcode.json";
                try {
                    CaptureVisionError error = cvRouter.initSettingsFromFile(templatePath);
                    if (error.getErrorCode() == EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
                        System.out.println("ErrorCode: " + error.getErrorCode() + ", ErrorString: " + error.getErrorString());
                    }
                } catch (CaptureVisionException e) {
                    System.out.println("Error: " + e.getErrorCode() + ", " + e.getErrorString());
                    continue;
                }

                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, "");
                if (results == null || results.length == 0) {
                    System.out.println("No parsed results.");
                } else {
                    for (int index = 0; index < results.length; index++) {
                        CapturedResult result = results[index];
                        if (result.getErrorCode() == EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
                            System.out.println("Warning: " + result.getErrorCode() + ", " + result.getErrorString());
                        } else if (result.getErrorCode() != EnumErrorCode.EC_OK) {
                            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
                        }

                        DecodedBarcodesResult barcodesResult = result.getDecodedBarcodesResult();
                        ParsedResult parsedResult = result.getParsedResult();
                        if (barcodesResult == null || barcodesResult.getItems().length == 0) {
                            System.out.println("Page-" + (index + 1) + " No parsed results.");
                        } else {
                            printResults(barcodesResult, parsedResult);
                        }
                    }
                }
            }
        } finally {
            scanner.close();
        }
    }
}