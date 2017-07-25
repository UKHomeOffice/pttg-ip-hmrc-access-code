package uk.gov.digital.ho.pttg.application;


public class TotpGeneratorTest {

}

//class TotpGeneratorSpec extends FunSpec {
//
//    private val secret: String = "-TOTP-SECRET-12345-"
//
//    private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
//
//    describe("A TOTP generator") {
//        it("should generate a different TOTP code for each 30 seconds window") {
//
//            val date1 = dateFormat.parse("2016-12-12 09:45:00.001").getTime
//            val date2 = dateFormat.parse("2016-12-12 09:45:29.999").getTime
//
//            val date3 = dateFormat.parse("2016-12-12 09:45:30.000").getTime
//            val date4 = dateFormat.parse("2016-12-12 09:45:59.999").getTime
//
//            val totpCode1 = TotpGenerator.getTotp(secret, date1)
//            val totpCode2 = TotpGenerator.getTotp(secret, date2)
//
//            val totpCode3 = TotpGenerator.getTotp(secret, date3)
//            val totpCode4 = TotpGenerator.getTotp(secret, date4)
//
//            assert(totpCode1 == totpCode2)
//            assert(totpCode2 != totpCode3)
//            assert(totpCode3 == totpCode4)
//        }
//    }