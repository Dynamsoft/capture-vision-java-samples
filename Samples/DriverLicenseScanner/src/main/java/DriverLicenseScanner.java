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
import java.util.Scanner;

class DriverLicenseResult {
    String codeType;
    String versionNumber;
    String licenseNumber;
    String vehicleClass;
    String fullName;
    String lastName;
    String givenName;
    String gender;
    String birthDate;
    String issuedDate;
    String expirationDate;

    public DriverLicenseResult(ParsedResultItem item) {
        codeType = item.getCodeType();
        if (!codeType.equals("AAMVA_DL_ID") && !codeType.equals("AAMVA_DL_ID_WITH_MAG_STRIPE") && !codeType.equals("SOUTH_AFRICA_DL")) {
            return;
        }
        if (item.getFieldValue("licenseNumber") != null && item.getFieldValidationStatus("licenseNumber") != EnumValidationStatus.VS_FAILED) {
            licenseNumber = item.getFieldValue("licenseNumber");
        }
        if (item.getFieldValue("AAMVAVersionNumber") != null && item.getFieldValidationStatus("AAMVAVersionNumber") != EnumValidationStatus.VS_FAILED) {
            versionNumber = item.getFieldValue("AAMVAVersionNumber");
        }
        if (item.getFieldValue("vehicleClass") != null && item.getFieldValidationStatus("vehicleClass") != EnumValidationStatus.VS_FAILED) {
            vehicleClass = item.getFieldValue("vehicleClass");
        }
        if (item.getFieldValue("lastName") != null && item.getFieldValidationStatus("lastName") != EnumValidationStatus.VS_FAILED) {
            lastName = item.getFieldValue("lastName");
        }
        if (item.getFieldValue("surName") != null && item.getFieldValidationStatus("surName") != EnumValidationStatus.VS_FAILED) {
            lastName = item.getFieldValue("surName");
        }
        if (item.getFieldValue("givenName") != null && item.getFieldValidationStatus("givenName") != EnumValidationStatus.VS_FAILED) {
            givenName = item.getFieldValue("givenName");
        }
        if (item.getFieldValue("fullName") != null && item.getFieldValidationStatus("fullName") != EnumValidationStatus.VS_FAILED) {
            fullName = item.getFieldValue("fullName");
        }
        if (item.getFieldValue("sex") != null && item.getFieldValidationStatus("sex") != EnumValidationStatus.VS_FAILED) {
            gender = item.getFieldValue("sex");
        }
        if (item.getFieldValue("gender") != null && item.getFieldValidationStatus("gender") != EnumValidationStatus.VS_FAILED) {
            gender = item.getFieldValue("gender");
        }
        if (item.getFieldValue("birthDate") != null && item.getFieldValidationStatus("birthDate") != EnumValidationStatus.VS_FAILED) {
            birthDate = item.getFieldValue("birthDate");
        }
        if (item.getFieldValue("issuedDate") != null && item.getFieldValidationStatus("issuedDate") != EnumValidationStatus.VS_FAILED) {
            issuedDate = item.getFieldValue("issuedDate");
        }
        if (item.getFieldValue("expirationDate") != null && item.getFieldValidationStatus("expirationDate") != EnumValidationStatus.VS_FAILED) {
            expirationDate = item.getFieldValue("expirationDate");
        }
        if (fullName == null) {
            fullName = (lastName != null ? lastName : "") + ((lastName != null && givenName != null) ? " " + givenName : (givenName != null ? givenName : ""));
        }
    }

    @Override
    public String toString() {
        return "Parsed Information:\n" +
                "\tCode Type: " + (codeType != null ? codeType : "") + "\n" +
                "\tLicense Number: " + (licenseNumber != null ? licenseNumber : "") + "\n" +
                "\tVehicle Class: " + (vehicleClass != null ? vehicleClass : "") + "\n" +
                "\tLast Name: " + (lastName != null ? lastName : "") + "\n" +
                "\tGiven Name: " + (givenName != null ? givenName : "") + "\n" +
                "\tFull Name: " + (fullName != null ? fullName : "") + "\n" +
                "\tGender: " + (gender != null ? gender : "") + "\n" +
                "\tDate of Birth: " + (birthDate != null ? birthDate : "") + "\n" +
                "\tIssued Date: " + (issuedDate != null ? issuedDate : "") + "\n" +
                "\tExpiration Date: " + (expirationDate != null ? expirationDate : "") + "\n";
    }
}

public class DriverLicenseScanner {
    private static void printResults(ParsedResult result) {
        ImageTag tag = result.getOriginalImageTag();
        if (tag instanceof FileImageTag) {
            System.out.println("File: " + ((FileImageTag)tag).getFilePath());
        }

        if (result.getErrorCode() != EnumErrorCode.EC_OK && result.getErrorCode() != EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Error: " + result.getErrorString());
        } else {
            ParsedResultItem[] items = result.getItems();
            System.out.println("Parsed " + items.length + " Driver License(s).");
            for (ParsedResultItem item : items) {
                DriverLicenseResult dlResult = new DriverLicenseResult(item);
                System.out.println(dlResult);
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
                    imagePath = "../../images/driver-license-sample.jpg";
                }

                imagePath = imagePath.replaceAll("^\"|\"$", "");
                if (Files.notExists(Paths.get(imagePath))) {
                    System.out.println("The image path does not exist.");
                    continue;
                }

                CapturedResult[] results = cvRouter.captureMultiPages(imagePath, "ReadDriversLicense");
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