import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

class StringPair {
    public String first;
    public String second;

    public StringPair(String first, String second) {
        this.first = first;
        this.second = second;
    }
}

public class DriverLicenseScanner {
    private static void printResults(ParsedResult result) {
        ImageTag tag = result.getOriginalImageTag();
        if (tag instanceof FileImageTag) {
            System.out.println("File: " + ((FileImageTag) tag).getFilePath());
        }

        if (result.getErrorCode() != EnumErrorCode.EC_OK
                && result.getErrorCode() != EnumErrorCode.EC_UNSUPPORTED_JSON_KEY_WARNING) {
            System.out.println("Error: " + result.getErrorString());
        } else {
            ParsedResultItem[] items = result.getItems();
            System.out.println("Parsed " + items.length + " Driver License(s).");
            for (ParsedResultItem item : items) {
                String codeType = item.getCodeType();
                List<StringPair> pairs = new ArrayList<>();
                switch (codeType) {
                    case "AAMVA_DL_ID": {
                        // For full field list and details, please refer to the documentation:
                        // https://www.dynamsoft.com/code-parser/docs/core/code-types/aamva-dl-id.html?lang=java#aamva_dl_id-fields

                        String licenseNumber = item.getFieldValue("licenseNumber");
                        if(licenseNumber != null && item.getFieldValidationStatus("licenseNumber") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("License Number", licenseNumber));
                        }
                        String vehicleClass = item.getFieldValue("vehicleClass");
                        if(vehicleClass != null && item.getFieldValidationStatus("vehicleClass") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Vehicle Class", vehicleClass));
                        }

                        String fullName = item.getFieldValue("fullName");
                        if (fullName == null || fullName.isEmpty() || item.getFieldValidationStatus("fullName") == EnumValidationStatus.VS_FAILED) {
                            String givenName = item.getFieldValue("givenName");
                            String lastName = item.getFieldValue("lastName");
                            if (givenName != null && item.getFieldValidationStatus("givenName") != EnumValidationStatus.VS_FAILED) {
                                fullName = givenName;
                                if (lastName != null && item.getFieldValidationStatus("lastName") != EnumValidationStatus.VS_FAILED) {
                                    fullName += " " + lastName;
                                }
                            } else {
                                String firstName = item.getFieldValue("firstName");
                                String middleName = item.getFieldValue("middleName");
                                if(firstName != null && item.getFieldValidationStatus("firstName") == EnumValidationStatus.VS_FAILED) {
                                    firstName = null;
                                }
                                if(middleName != null && item.getFieldValidationStatus("middleName") == EnumValidationStatus.VS_FAILED) {
                                    middleName = null;
                                }
                                if (firstName != null) {
                                    fullName = firstName;
                                    if (middleName != null) {
                                        fullName += " " + middleName;
                                    }
                                    if (lastName != null && item.getFieldValidationStatus("lastName") != EnumValidationStatus.VS_FAILED) {
                                        fullName += " " + lastName;
                                    }
                                }
                            }
                        }
                        if (fullName != null && !fullName.isEmpty()) {
                            pairs.add(new StringPair("Full Name", fullName));
                        }
                        String sex = item.getFieldValue("sex");
                        if (sex != null && item.getFieldValidationStatus("sex") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Sex", sex));
                        }
                        String expirationDate = item.getFieldValue("expirationDate");
                        if (expirationDate != null && item.getFieldValidationStatus("expirationDate") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Expiration Date", expirationDate));
                        }
                        break;
                    }
                    case "AAMVA_DL_ID_WITH_MAG_STRIPE": {
                        // For full field list and details, please refer to the documentation:
                        // https://www.dynamsoft.com/code-parser/docs/core/code-types/aamva-dl-id.html?lang=java#aamva_dl_id_with_mag_stripe-fields

                        String licenseNumber = item.getFieldValue("DLorID_Number");
                        if (licenseNumber != null && item.getFieldValidationStatus("DLorID_Number") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("License Number", licenseNumber));
                        }
                        String isoIIN = item.getFieldValue("ISOIIN");
                        if (isoIIN != null && item.getFieldValidationStatus("ISOIIN") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("ISO IIN", isoIIN));
                        }
                        String name = item.getFieldValue("name");
                        if (name != null && item.getFieldValidationStatus("name") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Name", name));
                        }
                        String sex = item.getFieldValue("sex");
                        if (sex != null && item.getFieldValidationStatus("sex") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Sex", sex));
                        }
                        String expirationDate = item.getFieldValue("expirationDate");
                        if (expirationDate != null && item.getFieldValidationStatus("expirationDate") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Expiration Date", expirationDate));
                        }
                        break;
                    }
                    case "SOUTH_AFRICA_DL": {
                        // For full field list and details, please refer to the documentation:
                        // https://www.dynamsoft.com/code-parser/docs/core/code-types/za-dl.html?lang=java#fields

                        String idNumber = item.getFieldValue("idNumber");
                        if (idNumber != null && item.getFieldValidationStatus("idNumber") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("ID Number", idNumber));
                        }
                        String licenseNumber = item.getFieldValue("licenseNumber");
                        if (licenseNumber != null && item.getFieldValidationStatus("licenseNumber") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("License Number", licenseNumber));
                        }
                        String vehicleLicense = item.getFieldValue("vehicleLicense");
                        if (vehicleLicense != null && item.getFieldValidationStatus("vehicleLicense") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Vehicle License", vehicleLicense));
                        }
                        String fullName = "";
                        String name = item.getFieldValue("surname");
                        String initials = item.getFieldValue("initials");
                        if (name != null && !name.isEmpty() && item.getFieldValidationStatus("surname") != EnumValidationStatus.VS_FAILED) {
                            fullName = name;
                            if (initials != null && !initials.isEmpty() && item.getFieldValidationStatus("initials") != EnumValidationStatus.VS_FAILED) {
                                fullName += " " + initials;
                            }
                        }
                        if (!fullName.isEmpty()) {
                            pairs.add(new StringPair("Name", fullName));
                        }
                        String gender = item.getFieldValue("gender");
                        if (gender != null && item.getFieldValidationStatus("gender") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("Gender", gender));
                        }
                        String licenseValidityTo = item.getFieldValue("licenseValidityTo");
                        if (licenseValidityTo != null && item.getFieldValidationStatus("licenseValidityTo") != EnumValidationStatus.VS_FAILED) {
                            pairs.add(new StringPair("License Valid To", licenseValidityTo));
                        }
                        break;
                    }
                    default:
                        System.out.println("Unsupported code type: " + codeType);
                        continue;
                }
                System.out.println("Parsed Information:");
                for(StringPair pair : pairs) {
                    if (pair.second != null && !pair.second.isEmpty()) {
                        System.out.println("\t" + pair.first + ": " + pair.second);
                    }
                }
                System.out.println("");
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
                    imagePath = "../../Images/driver-license-sample.jpg";
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

                        ImageTag tag = result.getOriginalImageTag();
                        int pageNumber = tag instanceof FileImageTag ? ((FileImageTag)tag).getPageNumber() : index;

                        ParsedResult parsedResult = result.getParsedResult();
                        if (parsedResult == null || parsedResult.getItems().length == 0) {
                            System.out.println("Page-" + (pageNumber + 1) + " No parsed results.");
                        } else {
                            System.out.println("Page-" + (pageNumber + 1) + " Parsed.");
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