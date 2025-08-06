import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.EnumPresetTemplate;
import com.dynamsoft.ddn.DeskewedImageResultItem;
import com.dynamsoft.ddn.ProcessedDocumentResult;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;
import com.dynamsoft.utility.ImageIO;
import com.dynamsoft.utility.UtilityException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class DocumentScanner {
    public static void main(String[] args) {
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
                System.out.println(">> Input your image full path:");
                System.out.println(">> 'Enter' for sample image or 'Q'/'q' to quit");
                String imagePath = scanner.nextLine();

                if (imagePath.equalsIgnoreCase("q")) {
                    return;
                }

                if (imagePath.isEmpty()) {
                    imagePath = "../../images/document-sample.jpg";
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (Files.notExists(Paths.get(imagePath))) {
                    System.out.println("The image path does not exist.");
                    continue;
                }

                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, EnumPresetTemplate.PT_DETECT_AND_NORMALIZE_DOCUMENT);
                if (results == null) {
                    System.out.println("No document found.");
                } else {
                    for (int index = 0; index < results.length; index++) {
                        CapturedResult result = results[index];
                        if (result.getErrorCode() == EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
                            System.out.println("Warning: " + result.getErrorCode() + ", " + result.getErrorString());
                        } else if (result.getErrorCode() != EnumErrorCode.EC_OK) {
                            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
                        }

                        ProcessedDocumentResult processedDocumentResult = result.getProcessedDocumentResult();
                        DeskewedImageResultItem[] items = processedDocumentResult != null ? processedDocumentResult.getDeskewedImageResultItems() : null;
                        if (items == null || items.length == 0) {
                            System.out.println("Page-" + (index + 1) + " No document found.");
                        } else {
                            System.out.println("Page-" + (index + 1) + " Deskewed " + items.length + " documents.");
                            for (int i = 0; i < items.length; i++) {
                                DeskewedImageResultItem item = items[i];
                                String outPath = "Page_" + (index + 1) + "deskewedResult_" + i + ".png";

                                ImageIO imageIO = new ImageIO();
                                try {
                                    imageIO.saveToFile(item.getImageData(), outPath);
                                } catch (UtilityException e) {
                                    continue;
                                }

                                System.out.println("Document " + (i + 1) + " file: " + outPath);
                            }
                        }
                    }
                }
            }
        } finally {
            scanner.close();
        }
    }
}