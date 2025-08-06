import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.core.basic_structures.FileImageTag;
import com.dynamsoft.core.basic_structures.ImageTag;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.dcp.EnumValidationStatus;
import com.dynamsoft.dcp.ParsedResult;
import com.dynamsoft.dcp.ParsedResultItem;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

class VINResult {
    String codeType;
    String wmi;
    String region;
    String vds;
    String vis;
    String modelYear;
    String plantCode;
    String vinString;

    public VINResult(ParsedResultItem item) {
        codeType = item.getCodeType();
        if (!Objects.equals(codeType, "VIN")) {
            return;
        }
        if (item.getFieldValue("vinString") != null) {
            vinString = item.getFieldValue("vinString");
            if (item.getFieldValidationStatus("vinString") == EnumValidationStatus.VS_FAILED) {
                vinString += ", Validation Failed";
            }
        }            if (item.getFieldValue("WMI") != null && item.getFieldValidationStatus("WMI") != EnumValidationStatus.VS_FAILED) {
            wmi = item.getFieldValue("WMI");
        }
        if (item.getFieldValue("region") != null && item.getFieldValidationStatus("region") != EnumValidationStatus.VS_FAILED) {
            region = item.getFieldValue("region");
        }
        if (item.getFieldValue("VDS") != null && item.getFieldValidationStatus("VDS") != EnumValidationStatus.VS_FAILED) {
            vds = item.getFieldValue("VDS");
        }
        if (item.getFieldValue("modelYear") != null && item.getFieldValidationStatus("modelYear") != EnumValidationStatus.VS_FAILED) {
            modelYear = item.getFieldValue("modelYear");
        }
        if (item.getFieldValue("plantCode") != null && item.getFieldValidationStatus("plantCode") != EnumValidationStatus.VS_FAILED) {
            plantCode = item.getFieldValue("plantCode");
        }
    }

    @Override
    public String toString() {
        return "VIN String: " + vinString + "\n" +
                "Parsed Information:\n" +
                "\tWMI: " + (wmi != null ? wmi : "") + "\n" +
                "\tRegion: " + (region != null ? region : "") + "\n" +
                "\tVDS: " + (vds != null ? vds : "") + "\n" +
                "\tVIS: " + (vis != null ? vis : "") + "\n" +
                "\tModelYear: " + (modelYear != null ? modelYear : "") + "\n" +
                "\tPlantCode: " + (plantCode != null ? plantCode : "") + "\n";
    }
}

public class VINScanner {
    private static void printResults(ParsedResult result) {
        ImageTag tag = result.getOriginalImageTag();
        if (tag instanceof FileImageTag) {
            System.out.println("File: " + ((FileImageTag)tag).getFilePath());
        }

        if (result.getErrorCode() != EnumErrorCode.EC_OK && result.getErrorCode() != EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Error: " + result.getErrorString());
        } else {
            ParsedResultItem[] items = result.getItems();
            System.out.println("Parsed " + items.length + " VIN codes.");
            for (ParsedResultItem item : items) {
                VINResult vinResult = new VINResult(item);
                System.out.println(vinResult);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("**********************************************************");
        System.out.println("Welcome to Dynamsoft Capture Vision - VIN Sample");
        System.out.println("**********************************************************");

        Scanner scanner = new Scanner(System.in);

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
                System.out.println(">> Enter the image path (or 'Q'/'q' to quit):");
                String imagePath = scanner.nextLine();
                if (imagePath.isEmpty()) {
                    System.out.println("Invalid path.");
                    continue;
                }

                if (imagePath.equalsIgnoreCase("q")) {
                    return;
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (Files.notExists(Paths.get(imagePath))) {
                    System.out.println("The image path does not exist.");
                    continue;
                }

                int mode = 0;
                String templateName = null;
                while (true) {
                    System.out.println(">> Step 2: Choose a Mode Number:");
                    System.out.println("1. Read VIN from Barcode");
                    System.out.println("2. Read VIN from Text");

                    try {
                        mode = scanner.nextInt();
                        scanner.nextLine();
                        if (mode == 1 || mode == 2) {
                            templateName = mode == 1 ? "ReadVINBarcode" : "ReadVINText";
                            break;
                        }
                    } catch (Exception ignored) {
                        scanner.nextLine();
                    }
                }

                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, templateName);
                if (results == null || results.length == 0) {
                    System.out.println("No results.");
                } else {
                    for (int index = 0; index < results.length; index++) {
                        CapturedResult result = results[index];
                        if (result.getErrorCode() == EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
                            System.out.println("Warning: " + result.getErrorCode() + ", " + result.getErrorString());
                        } else if (result.getErrorCode() != EnumErrorCode.EC_OK) {
                            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
                        }

                        ParsedResult parsedResult = result.getParsedResult();
                        if (parsedResult == null || parsedResult.getItems().length == 0) {
                            System.out.println("Page-" + (index + 1) + " No parsed results.");
                        } else {
                            printResults(parsedResult);
                        }
                    }
                }
            }
        } finally {
            scanner.close();
        }
    }
}