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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    List<String> rawText;

    public MRZResult(ParsedResultItem item) {
        docType = item.getCodeType();
        if (docType.equals("MRTD_TD3_PASSPORT")) {
            if (item.getFieldValue("passportNumber") != null && item.getFieldValidationStatus("passportNumber") != EnumValidationStatus.VS_FAILED) {
                docId = item.getFieldValue("passportNumber");
            } else if (item.getFieldValue("documentNumber") != null && item.getFieldValidationStatus("documentNumber") != EnumValidationStatus.VS_FAILED) {
                docId = item.getFieldValue("documentNumber");
            }
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
    private static void printResults(ParsedResult result) {
        ImageTag tag = result.getOriginalImageTag();
        if (tag instanceof FileImageTag) {
            System.out.println("File: " + ((FileImageTag)tag).getFilePath());
        }

        if (result.getErrorCode() != EnumErrorCode.EC_OK && result.getErrorCode() != EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Error: " + result.getErrorString());
        } else {
            ParsedResultItem[] items = result.getItems();
            System.out.println("Parsed " + items.length + " MRZ Zones.");
            for (ParsedResultItem item : items) {
                MRZResult mrzResult = new MRZResult(item);
                System.out.print(mrzResult);
            }
        }
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
                    imagePath = "../../Images/passport-sample.jpg";
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (Files.notExists(Paths.get(imagePath))) {
                    System.out.println("The image path does not exist.");
                    continue;
                }

                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, "ReadPassportAndId");
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