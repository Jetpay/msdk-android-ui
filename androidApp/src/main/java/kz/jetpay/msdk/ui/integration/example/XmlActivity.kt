package kz.jetpay.msdk.ui.integration.example

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kz.jetpay.msdk.ui.integration.example.utils.CommonUtils
import kz.jetpay.msdk.ui.integration.example.utils.SignatureGenerator
import com.paymentpage.msdk.core.domain.entities.payment.Payment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kz.jetpay.msdk.ui.JetPayActionType
import kz.jetpay.msdk.ui.JetPayAdditionalField
import kz.jetpay.msdk.ui.JetPayAdditionalFieldType
import kz.jetpay.msdk.ui.JetPayPaymentInfo
import kz.jetpay.msdk.ui.JetPayPaymentSDK
import kz.jetpay.msdk.ui.JetPayRecipientInfo
import kz.jetpay.msdk.ui.JetPayRecurrentData
import kz.jetpay.msdk.ui.JetPayScreenDisplayMode
import kz.jetpay.msdk.ui.paymentOptions

class XmlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml)

        //1. Create JetPayPaymentInfo object
        val JetPayPaymentInfo = JetPayPaymentInfo(
            //required fields
            projectId = BuildConfig.PROJECT_ID, //Unique project Id
            paymentId = CommonUtils.getRandomPaymentId(),
            paymentAmount = 100, //1.00
            paymentCurrency = "USD",
            //optional fields
//            paymentDescription = "Test description",
//            customerId = "12",
//            regionCode = "",
//            token = "",
//            languageCode = "en",
//            receiptData = "",
//            hideSavedWallets = false,
//            forcePaymentMethod = "card",
//            JetPayThreeDSecureInfo = JetPayThreeDSecureInfo()
        )

        //2. Sign it
        JetPayPaymentInfo.signature = SignatureGenerator.generateSignature(
            paramsToSign = JetPayPaymentInfo.getParamsForSignature(),
            secret = BuildConfig.PROJECT_SECRET_KEY
        )

        //3. Configure SDK
        val paymentOptions = paymentOptions {
            //Required object for payment
            paymentInfo = JetPayPaymentInfo

            //Optional objects for payment
            //JetPayActionType.Sale by default
            actionType = JetPayActionType.Sale
            //GooglePay options
            isTestEnvironment = true
            merchantId = BuildConfig.GPAY_MERCHANT_ID
            merchantName = "Example Merchant Name"
            additionalFields {
                field {
                    JetPayAdditionalField(
                        JetPayAdditionalFieldType.CUSTOMER_EMAIL,
                        "mail@mail.com"
                    )
                }
                field {
                    JetPayAdditionalField(
                        JetPayAdditionalFieldType.CUSTOMER_FIRST_NAME,
                        "firstName"
                    )
                }
            }
            screenDisplayModes {
                mode(JetPayScreenDisplayMode.HIDE_SUCCESS_FINAL_SCREEN)
                mode(JetPayScreenDisplayMode.HIDE_DECLINE_FINAL_SCREEN)
            }
            recurrentData = JetPayRecurrentData()
            recipientInfo = JetPayRecipientInfo()

            //Parameter to enable hiding or displaying scanning cards feature
            hideScanningCards = false

            //Custom theme
            isDarkTheme = false
        }

        //4. Create sdk object
        val sdk = JetPayPaymentSDK(
            context = applicationContext,
            paymentOptions = paymentOptions,
            mockModeType = JetPayPaymentSDK.JetPayMockModeType.SUCCESS
        )

        //5. Open it
        startActivityForResult.launch(sdk.intent)
    }

    //6. Handle result
    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            when (result.resultCode) {
                JetPayPaymentSDK.RESULT_SUCCESS -> {
                    val payment =
                        Json.decodeFromString<Payment?>(
                            data?.getStringExtra(
                                JetPayPaymentSDK.EXTRA_PAYMENT
                            ).toString()
                        )
                    when {
                        payment?.token != null -> {
                            Toast.makeText(
                                this,
                                "Tokenization was finished successfully. Your token is ${payment.token}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(
                                "PaymentSDK",
                                "Tokenization was finished successfully. Your token is ${payment.token}"
                            )
                        }
                        else -> {
                            Toast.makeText(
                                this,
                                "Payment was finished successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("PaymentSDK", "Payment was finished successfully")
                        }
                    }

                }
                JetPayPaymentSDK.RESULT_CANCELLED -> {
                    Toast.makeText(this, "Payment was cancelled", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentSDK", "Payment was cancelled")
                }
                JetPayPaymentSDK.RESULT_DECLINE -> {
                    Toast.makeText(this, "Payment was declined", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentSDK", "Payment was declined")
                }
                JetPayPaymentSDK.RESULT_ERROR -> {
                    val errorCode = data?.getStringExtra(JetPayPaymentSDK.EXTRA_ERROR_CODE)
                    val message = data?.getStringExtra(JetPayPaymentSDK.EXTRA_ERROR_MESSAGE)
                    Toast.makeText(
                        this,
                        "Payment was interrupted. See logs",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(
                        "PaymentSDK",
                        "Payment was interrupted. Error code: $errorCode. Message: $message"
                    )
                }
            }
        }
}
