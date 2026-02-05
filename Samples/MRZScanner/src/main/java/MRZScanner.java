import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.dynamsoft.core.CoreException;
import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.core.IntermediateResultExtraInfo;
import com.dynamsoft.core.basic_structures.CapturedResultItem;
import com.dynamsoft.core.basic_structures.FileImageTag;
import com.dynamsoft.core.basic_structures.ImageData;
import com.dynamsoft.core.basic_structures.OriginalImageResultItem;
import com.dynamsoft.core.basic_structures.Quadrilateral;
import com.dynamsoft.core.intermediate_results.ScaledColourImageUnit;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.IntermediateResultManager;
import com.dynamsoft.cvr.IntermediateResultReceiver;
import com.dynamsoft.dcp.EnumValidationStatus;
import com.dynamsoft.dcp.ParsedResult;
import com.dynamsoft.dcp.ParsedResultItem;
import com.dynamsoft.ddn.EnhancedImageResultItem;
import com.dynamsoft.ddn.ProcessedDocumentResult;
import com.dynamsoft.ddn.intermediate_results.DeskewedImageElement;
import com.dynamsoft.ddn.intermediate_results.DeskewedImageUnit;
import com.dynamsoft.ddn.intermediate_results.DetectedQuadsUnit;
import com.dynamsoft.dlr.intermediate_results.LocalizedTextLinesUnit;
import com.dynamsoft.dlr.intermediate_results.RecognizedTextLinesUnit;
import com.dynamsoft.id_utility.IdentityProcessor;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;
import com.dynamsoft.utility.ImageIO;
import com.dynamsoft.utility.ImageProcessor;

class PortraitZoneData {
    public ScaledColourImageUnit scaledColourImageUnit;
    public LocalizedTextLinesUnit localizedTextLinesUnit;
    public RecognizedTextLinesUnit recognizedTextLinesUnit;
    public DetectedQuadsUnit detectedQuadsUnit;
    public DeskewedImageUnit deskewedImageUnit;
}

class MyIntermediateResultReceiver extends IntermediateResultReceiver {
    private ConcurrentHashMap<String, PortraitZoneData> portraitZoneDataMap = new ConcurrentHashMap<String, PortraitZoneData>();

    @Override
    public void onScaledColourImageUnitReceived(ScaledColourImageUnit result, IntermediateResultExtraInfo info) {
        String hashId = result.getOriginalImageHashId();
        PortraitZoneData data = getData(hashId);
        data.scaledColourImageUnit = result;
    }

    @Override
    public void onLocalizedTextLinesReceived(LocalizedTextLinesUnit result, IntermediateResultExtraInfo info) {
        if (info.isSectionLevelResult) {
            String hashId = result.getOriginalImageHashId();
            PortraitZoneData data = getData(hashId);
            data.localizedTextLinesUnit = result;
        }
    }

    @Override
    public void onRecognizedTextLinesReceived(RecognizedTextLinesUnit result, IntermediateResultExtraInfo info) {
        if (info.isSectionLevelResult) {
            String hashId = result.getOriginalImageHashId();
            PortraitZoneData data = getData(hashId);
            data.recognizedTextLinesUnit = result;
        }
    }

    @Override
    public void onDetectedQuadsReceived(DetectedQuadsUnit result, IntermediateResultExtraInfo info) {
        if (!info.isSectionLevelResult) {
            String hashId = result.getOriginalImageHashId();
            PortraitZoneData data = getData(hashId);
            data.detectedQuadsUnit = result;
        }
    }

    @Override
    public void onDeskewedImageReceived(DeskewedImageUnit result, IntermediateResultExtraInfo info) {
        if (info.isSectionLevelResult) {
            String hashId = result.getOriginalImageHashId();
            PortraitZoneData data = getData(hashId);
            data.deskewedImageUnit = result;
        }
    }

    public Quadrilateral getPortraitZone(String hashId) throws CoreException {
        PortraitZoneData data = getData(hashId);
        if (data != null) {
            IdentityProcessor idProcessor = new IdentityProcessor();
            return idProcessor.findPortraitZone(
                    data.scaledColourImageUnit,
                    data.localizedTextLinesUnit,
                    data.recognizedTextLinesUnit,
                    data.detectedQuadsUnit,
                    data.deskewedImageUnit);
        }
        return null;
    }

    private PortraitZoneData getData(String hashId) {
        return portraitZoneDataMap.computeIfAbsent(hashId, k -> new PortraitZoneData());
    }
}

class MRZResult {
    String docId;
    String docType;
    String nationality;
    String issuer;
    String dateOfBirth;
    String dateOfExpiry;
    String gender;
    String surname;
    String givenName;
    boolean isPassport;
    List<String> rawText;

    public MRZResult(ParsedResultItem item) {
        docType = item.getCodeType();
        if (docType.equals("MRTD_TD3_PASSPORT")) {
            if (item.getFieldValue("passportNumber") != null && item.getFieldValidationStatus("passportNumber") != EnumValidationStatus.VS_FAILED) {
                docId = item.getFieldValue("passportNumber");
            }
            isPassport = true;
        } else if (item.getFieldValue("documentNumber") != null && item.getFieldValidationStatus("documentNumber") != EnumValidationStatus.VS_FAILED) {
            docId = item.getFieldValue("documentNumber");
        }

        rawText = new ArrayList<>();
        String line = item.getFieldValue("line1");
        if (line != null) {
            if (item.getFieldValidationStatus("line1") == EnumValidationStatus.VS_FAILED) {
                line += ", Validation Failed";
            }
            rawText.add(line);
        }
        line = item.getFieldValue("line2");
        if (line != null) {
            if (item.getFieldValidationStatus("line2") == EnumValidationStatus.VS_FAILED) {
                line += ", Validation Failed";
            }
            rawText.add(line);
        }
        line = item.getFieldValue("line3");
        if (line != null) {
            if (item.getFieldValidationStatus("line3") == EnumValidationStatus.VS_FAILED) {
                line += ", Validation Failed";
            }
            rawText.add(line);
        }

        if (item.getFieldValue("nationality") != null && item.getFieldValidationStatus("nationality") != EnumValidationStatus.VS_FAILED) {
            nationality = item.getFieldValue("nationality");
        }
        if (item.getFieldValue("issuingState") != null && item.getFieldValidationStatus("issuingState") != EnumValidationStatus.VS_FAILED) {
            issuer = item.getFieldValue("issuingState");
        }
        if (item.getFieldValue("dateOfBirth") != null && item.getFieldValidationStatus("dateOfBirth") != EnumValidationStatus.VS_FAILED) {
            dateOfBirth = item.getFieldValue("dateOfBirth");
        }
        if (item.getFieldValue("dateOfExpiry") != null && item.getFieldValidationStatus("dateOfExpiry") != EnumValidationStatus.VS_FAILED) {
            dateOfExpiry = item.getFieldValue("dateOfExpiry");
        }
        if (item.getFieldValue("sex") != null && item.getFieldValidationStatus("sex") != EnumValidationStatus.VS_FAILED) {
            gender = item.getFieldValue("sex");
        }
        if (item.getFieldValue("primaryIdentifier") != null && item.getFieldValidationStatus("primaryIdentifier") != EnumValidationStatus.VS_FAILED) {
            surname = item.getFieldValue("primaryIdentifier");
        }
        if (item.getFieldValue("secondaryIdentifier") != null && item.getFieldValidationStatus("secondaryIdentifier") != EnumValidationStatus.VS_FAILED) {
            givenName = item.getFieldValue("secondaryIdentifier");
        }
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder("Raw Text:\n");
        for (int index = 0; index < rawText.size(); index++) {
            msg.append("\tLine ").append(index + 1).append(": ").append(rawText.get(index)).append("\n");
        }
        msg.append("Parsed Information:\n")
                .append("\tDocument Type: ").append(docType != null ? docType : "").append("\n")
                .append("\tDocument ID: ").append(docId != null ? docId : "").append("\n")
                .append("\tSurname: ").append(surname != null ? surname : "").append("\n")
                .append("\tGiven Name: ").append(givenName != null ? givenName : "").append("\n")
                .append("\tNationality: ").append(nationality != null ? nationality : "").append("\n")
                .append("\tIssuing Country or Organization: ").append(issuer != null ? issuer : "").append("\n")
                .append("\tGender: ").append(gender != null ? gender : "").append("\n")
                .append("\tDate of Birth(YYMMDD): ").append(dateOfBirth != null ? dateOfBirth : "").append("\n")
                .append("\tExpiration Date(YYMMDD): ").append(dateOfExpiry != null ? dateOfExpiry : "").append("\n");
        return msg.toString();
    }
}

public class MRZScanner {

    private static ImageData getOriginalImage(CapturedResult result) {
        CapturedResultItem[] items = result.getItems();
        for (CapturedResultItem item : items) {
            if (item instanceof OriginalImageResultItem) {
                OriginalImageResultItem originalImageItem = (OriginalImageResultItem)item;
                return originalImageItem.getImageData();
            }
        }
        return null;
    }

    private static void saveProcessedDocumentResult(CapturedResult result, int pageNumber, String imagePathPrefix) {
        System.out.println("Extract and save the normalized document image.");

        ProcessedDocumentResult docResult = result.getProcessedDocumentResult();
        EnhancedImageResultItem[] enhancedImageResultItems = docResult != null ? docResult.getEnhancedImageResultItems() : null;
        if (enhancedImageResultItems == null || enhancedImageResultItems.length == 0) {
            System.out.println("Page-" + pageNumber + " No processed document result found.");
            return;
        }

        EnhancedImageResultItem enhancedImageResultItem = enhancedImageResultItems[0];
        String outputPath = imagePathPrefix + pageNumber + "_document.png";
        ImageIO imgIO = new ImageIO();
        ImageData enhancedImage = enhancedImageResultItem.getImageData();
        if (enhancedImage != null) {
            try {
                imgIO.saveToFile(enhancedImage, outputPath);
                System.out.println("Document file: " + outputPath);
            } catch (CoreException e) {
                System.out.println("Save document file failed, error: " + e.getErrorCode() + ", " + e.getErrorString());
            }
        }
    }

    private static void savePortraitZone(MyIntermediateResultReceiver irReceiver, String hashId, ImageData originImageData, int pageNumber, String imagePathPrefix) {
        System.out.println("Extract and save the portrait zone image.");

        if (originImageData == null) {
            System.out.println("Page-" + pageNumber + " Original image data not exists.");
            return;
        }

        Quadrilateral quad;
        try {
            quad = irReceiver.getPortraitZone(hashId);
            if (quad == null) {
                System.out.println("Page-" + pageNumber + " No portrait zone found.");
                return;
            }
        } catch (CoreException e) {
            System.out.println("Finding portrait zone failed, error: " + e.getErrorCode() + ", " + e.getErrorString());
            return;
        }

        ImageProcessor imgProcessor = new ImageProcessor();
        ImageData croppedImage;
        try {
            croppedImage = imgProcessor.cropAndDeskewImage(originImageData, quad);
        } catch (CoreException e) {
            System.out.println("Crop image failed, error: " + e.getErrorCode() + ", " + e.getErrorString());
            return;
        }

        String outputPath = imagePathPrefix + pageNumber + "_portrait.png";
        ImageIO imgIO = new ImageIO();
        try {
            imgIO.saveToFile(croppedImage, outputPath);
            System.out.println("Portrait file: " + outputPath);
        } catch (CoreException e) {
            System.out.println("Save portrait file failed, error: " + e.getErrorCode() + ", " + e.getErrorString());
        }
    }

    private static void processResult(CapturedResult result, MyIntermediateResultReceiver irReceiver, int printIndex) {
        if (result.getErrorCode() == EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Warning: " + result.getErrorCode() + ", " + result.getErrorString());
        } else if (result.getErrorCode() != EnumErrorCode.EC_OK) {
            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
        }

        int pageNumber = printIndex + 1;
        String imagePathPrefix = "";

        FileImageTag tag = (FileImageTag)result.getOriginalImageTag();
        if (tag != null) {
            imagePathPrefix = Paths.get(tag.getFilePath()).getFileName().toString().replaceFirst("[.][^.]+$", "") + "_";

            pageNumber = tag.getPageNumber() + 1;
            System.out.println("File: " + tag.getFilePath());
            System.out.println("Page Number: " + pageNumber);
        }

        ParsedResult parsedResult = result.getParsedResult();
        ParsedResultItem[] parsedResultItems = parsedResult != null ? parsedResult.getItems() : null;
        if (parsedResultItems == null || parsedResultItems.length == 0) {
            System.out.println("No parsed results in page " + pageNumber + ".");
            return;
        }

        String hashId = result.getOriginalImageHashId();
        boolean isPassport = false;
        if (parsedResult.getErrorCode() != EnumErrorCode.EC_OK && parsedResult.getErrorCode() != EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Error: " + parsedResult.getErrorCode() + ", " + parsedResult.getErrorString());
        } else {
            for (ParsedResultItem parsedResultItem : parsedResultItems) {
                MRZResult mrzResult = new MRZResult(parsedResultItem);
                System.out.println(mrzResult);
                if (!isPassport)
                    isPassport = mrzResult.isPassport;
            }
        }

        if (isPassport) {
            ImageData originalImage = getOriginalImage(result);
            saveProcessedDocumentResult(result, pageNumber, imagePathPrefix);
            savePortraitZone(irReceiver, hashId, originalImage, pageNumber, imagePathPrefix);
        }

        System.out.println();
    }

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
                if (licenseError.getErrorCode() != EnumErrorCode.EC_OK && licenseError.getErrorCode() != EnumErrorCode.EC_LICENSE_WARNING) {
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
            IntermediateResultManager irManager = cvRouter.getIntermediateResultManager();
            while (true) {
                System.out.println(">> Input your image full path:");
                System.out.println(">> 'Enter' for sample image or 'Q'/'q' to quit");
                String imagePath = scanner.nextLine();

                if (imagePath.equalsIgnoreCase("q")) {
                    return;
                }

                if (imagePath.isEmpty()) {
                    imagePath = "../../Images/passport-sample.jpg";
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (Files.notExists(Paths.get(imagePath))) {
                    System.out.println("The image path does not exist.");
                    continue;
                }

                MyIntermediateResultReceiver irReceiver = new MyIntermediateResultReceiver();
                irManager.addResultReceiver(irReceiver);
                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, "ReadPassportAndId");
                irManager.removeResultReceiver(irReceiver);

                if (results == null || results.length == 0) {
                    System.out.println("No results.");
                } else {
                    for (int index = 0; index < results.length; index++) {
                        processResult(results[index], irReceiver, index);
                    }
                }
            }
        } catch (CoreException e) {
            System.out.println("Error: " + e.getErrorCode() + ", " + e.getErrorString());
        } finally {
            scanner.close();
        }
    }
}