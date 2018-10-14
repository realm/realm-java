package io.realm;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TrustManagerCertificateValidationTests {

    @BeforeClass
    public static void setUp() {
        // mainly to setup logging otherwise
        // java.lang.UnsatisfiedLinkError: No implementation found for void io.realm.log.RealmLog.nativeSetLogLevel(int) (tried Java_io_realm_log_RealmLog_nativeSetLogLevel and Java_io_realm_log_RealmLog_nativeSetLogLevel__I)
        // will be thrown
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    // IMPORTANT: Following test assume the root certificate is installed on the test device
    //            certificate is located in <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt
    //            adb push <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt /sdcard/
    //            then import the certificate from the device (Settings/Security/Install from storage)
    @Test
    public void sslVerifyCallback_certificateChainWithRootCAInstalledShouldValidate() {
        // simulating the following certificate chain
        // ---
        // Certificate chain
        // 0 s:/DC=127.0.0.1/O=Realm/OU=Realm/CN=127.0.0.1
        // i:/DC=io/DC=realm/O=Realm/OU=Realm Test Signing CA/CN=Realm Test Signing CA
        // 1 s:/DC=io/DC=realm/O=Realm/OU=Realm Test Signing CA/CN=Realm Test Signing CA
        // i:/DC=io/DC=realm/O=Realm/OU=Realm Test Root CA/CN=Realm Test Root CA
        // ---

        // s:/DC=127.0.0.1/O=Realm/OU=Realm/CN=127.0.0.1
        String pem_depth0 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIE1DCCArygAwIBAgIBBzANBgkqhkiG9w0BAQUFADB7MRIwEAYKCZImiZPyLGQB\n" +
                "GRYCaW8xFTATBgoJkiaJk/IsZAEZFgVyZWFsbTEOMAwGA1UECgwFUmVhbG0xHjAc\n" +
                "BgNVBAsMFVJlYWxtIFRlc3QgU2lnbmluZyBDQTEeMBwGA1UEAwwVUmVhbG0gVGVz\n" +
                "dCBTaWduaW5nIENBMB4XDTE3MDUxNzIzMjg0OFoXDTE5MDUxNzIzMjg0OFowTzEZ\n" +
                "MBcGCgmSJomT8ixkARkWCTEyNy4wLjAuMTEOMAwGA1UECgwFUmVhbG0xDjAMBgNV\n" +
                "BAsMBVJlYWxtMRIwEAYDVQQDDAkxMjcuMC4wLjEwggEiMA0GCSqGSIb3DQEBAQUA\n" +
                "A4IBDwAwggEKAoIBAQC3jJl7a1spgJyZt/64HgZsTVi9OLbME2r//fYmoHHSipTq\n" +
                "Br7huFsDXpaOYRkPgF+4UUOXADhnRw4JuKuA0ZyBuIHbC7TF3no89ZzLvysS/rGd\n" +
                "TqBKq67EERlUxRftWMNy8OVG3CFBTGMdMYXzuvataT7Yhp3EVjtSR10k3UCv+foD\n" +
                "TE4tW9I03PCkGRMU9mx8HEe9fXmiCWGtP41OWcWupys5AOk0aGxv2GCiqSQzHJ+A\n" +
                "tMaOujeYcT3dgmbY4MKBzEvRXVgmz4UKrP0IpUBQ//lz6CcYe3B1cyojx9cVvsrO\n" +
                "V8nuu2202P3HIkcomwBeS6+CY8PXanROYBeUavuDAgMBAAGjgY4wgYswDgYDVR0P\n" +
                "AQH/BAQDAgWgMAkGA1UdEwQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUF\n" +
                "BwMCMB0GA1UdDgQWBBTGvfRJ9S52UkTx4s4ubPlZsVYUrTAfBgNVHSMEGDAWgBQn\n" +
                "eeHa8RXQ6eWGMIfnH1/PJzpwtDAPBgNVHREECDAGhwR/AAABMA0GCSqGSIb3DQEB\n" +
                "BQUAA4ICAQCbP3T0aXJrW3WItxBf4HOygr7ccRuj1qRurqZfUXhcgGQgISATFgjQ\n" +
                "rhX2UiTZI1wk7WI7DuZfAEu/oZQ0KvsqRl9U5jt/voFb3+h4ph7O4oe5i+TYBB8Y\n" +
                "xCmAeiGpVsUp7k4oM/qNkkaiMTHF+TEZ7R32x3WCZbYarbw0SvMYBaCj1JpQ8u+7\n" +
                "xC+JEJVoF2qFds6IjBnP16pww9BZm5rA0KjQ08318I5eGauhrlTcB6xtbtjw7mVH\n" +
                "3ikedhsdDmL13R32bq0nLo2+xKhBC7FEIj0ps1d0PjtBKBmNSO1lBVuOF6erRSTZ\n" +
                "lQDkBOds2GtrKoleH/u08hwgVer1QJlYot7Dg+UBcPhT6Y2Vugsg0JnmtDEFVQCc\n" +
                "9/OWfHRbfcdqruyQ+A/y8FjsgAx5BLDzac3lQfL1/ES62U8/Mv5p824fMpRieBd2\n" +
                "3NUMGaaLl3DpGTmo+rEAphhvSy04Lx2WC4eYhsEsdUQ8DuHr9MROAsef98wwinIj\n" +
                "v0R8fD/3fLGx16pL5B7dyv1ajS6q/0mvpWNviDEmfbOk401NRdZEexKobga7gcCA\n" +
                "pF+VO9SlSgEdAA57XSApl9DWiHPxicEBVIWbnO9Bbfm2g8xlrDTKv4j8NE9/YjDi\n" +
                "2QLrx1iGkG/kfl8gRfLEoH6tklqFjwiQPehlvlR54mI8XY5XNioXuw==\n" +
                "-----END CERTIFICATE-----";

        // s:/DC=io/DC=realm/O=Realm/OU=Realm Test Signing CA/CN=Realm Test Signing CA
        String pem_depth1 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIF0TCCA7mgAwIBAgIBAjANBgkqhkiG9w0BAQUFADB1MRIwEAYKCZImiZPyLGQB\n" +
                "GRYCaW8xFTATBgoJkiaJk/IsZAEZFgVyZWFsbTEOMAwGA1UECgwFUmVhbG0xGzAZ\n" +
                "BgNVBAsMElJlYWxtIFRlc3QgUm9vdCBDQTEbMBkGA1UEAwwSUmVhbG0gVGVzdCBS\n" +
                "b290IENBMB4XDTE2MDkwNzEwMTcyOFoXDTI2MDkwNzEwMTcyOFowezESMBAGCgmS\n" +
                "JomT8ixkARkWAmlvMRUwEwYKCZImiZPyLGQBGRYFcmVhbG0xDjAMBgNVBAoMBVJl\n" +
                "YWxtMR4wHAYDVQQLDBVSZWFsbSBUZXN0IFNpZ25pbmcgQ0ExHjAcBgNVBAMMFVJl\n" +
                "YWxtIFRlc3QgU2lnbmluZyBDQTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoC\n" +
                "ggIBAL9bWpLeU69zgOE/IlV1OH2eO2VJqtOnrAS+TaXCfQMwydhB0gAKzd+jaKUT\n" +
                "kgpxIsUJ1HWXc6b6N2SnYVWEiMG+65LgphsABMQx/UrpFFbIrQtcc8hVHOZgsTrj\n" +
                "wh1BGm1XEt/awv5A59GlcSlxyw0S1ca+6KtinBFwtd7xILa8Ba96P+TfdDPWu6Mz\n" +
                "WfM6oK8t6ucWyI8l8fsnc4BG40RbuPVMuo5hbV8swI/o0r066A36Ft4yGYTIbK0R\n" +
                "FFzORL5GvvB7gych8Un1uuW8WQewwvtPflZ268sU8VDWs4MQK7HTgGiYRWdwnhvv\n" +
                "/yjQ7xo4KGQWhFrRnwV/FVBqzqwIJeQ/1t8J2VmyBdm345Su9sYEaS7VR3lUkvty\n" +
                "8kwJK2Q6PtEwdgwzZQoIVTREgwXpHlHCWHBEMGzvCuCw4hAr4VUpJANoYbtEWOqt\n" +
                "A7OpDxNE/+ok03u9JXhXeXvkS568MjNj1fclOffFMY2f8naja7tbpN3MlkS0RJ1Q\n" +
                "7y5kKQKjx1L3NpLF+vt13SVnPkY3453c3vblagqVfumQPsmx+HQHuf/yJMmE8J88\n" +
                "p87KZL53HnyTKW/Ijo1006gd4dubi8Mn2A0D/H4+JRlquKWX0HrDEzO8OozHJen5\n" +
                "z0rFwyZjQu9Y10IGMIogyM1qQIv6iOBU7WAJaSYSQ7Xyk2xbAgMBAAGjZjBkMA4G\n" +
                "A1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBQneeHa\n" +
                "8RXQ6eWGMIfnH1/PJzpwtDAfBgNVHSMEGDAWgBSEcHEsBDvQkoO1+3x/sGEMYhZx\n" +
                "dDANBgkqhkiG9w0BAQUFAAOCAgEANgWEjIghCKfivUGoJ3+3wpqG1yH+7UxR0Snf\n" +
                "NUoO6qC1bMwoL169n5dovqoq/1SRnu8EXQ3s55g1EHhQth8XlqlemmD7aOkGfVOM\n" +
                "WLeaR+CfyNFDGnRBP6sDITWIjjQ6JbeYZySL1BSIVxyZ3wgMvVefU9s6R6TlTCk4\n" +
                "4oI5RepiyhvYlcsK42UQl8cQ14st2/oWxsQMgSbmb/Ha+3nAEidYmiuVoL1ziK31\n" +
                "rZvNST2tLAKE+Ii+PL/XoijoCR58DbBWrebjpxFWWGaD3YAxVqYVReHjUkny+Ew8\n" +
                "YP3WG0Vh7FLB2bnasF1cO3/vNN1IJhlaZq21p4drc+jq013N0T+sd+RZjU2VOC/o\n" +
                "F/+PZ8j4XY6Gt3hQJWI1uQcV9utlmICWC9IUy1QadQyr2cKZGyDa46R3aO91zER/\n" +
                "ZvRHjHoDIbZsxwCyUBWEXIcq+wM61y3fUpaAtsA9oEtlZ17zvUH+9GI63g8wjUe/\n" +
                "igv4Dth7hJNg5nOpYBHzWhYsKljA3HiPZsgQkNXaAzXppyKKBBTP4fvJRl/MKe/H\n" +
                "Ir1lpIpH4NUQDRJMo3IR5l+eW4c460h03YYmq0VhY0VSIak1ZYQwSYVokLYjDPAQ\n" +
                "ft7h6D2Ubf9EoC6GHEy77HKFO9BtSWlHqWEfxTnL1noG6UFS3wAAwAg/Ib1EUsR4\n" +
                "pf7lM/4=\n" +
                "-----END CERTIFICATE-----\n";

        String serverAddress = "127.0.0.1";

        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth1, 1));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth0, 0));
    }

    @Test
    public void sslVerifyCallback_shouldFailOnExpiredCert() {
        // simulating the following certificate chain (one of the
        // ---
        // Certificate chain
        // 0 s:/CN=*.ie1.realmlab.net
        // i:/C=US/O=Amazon/OU=Server CA 1B/CN=Amazon
        // 1 s:/C=US/O=Amazon/OU=Server CA 1B/CN=Amazon
        // i:/C=US/O=Amazon/CN=Amazon Root CA 1
        // 2 s:/C=US/O=Amazon/CN=Amazon Root CA 1
        // i:/C=US/ST=Arizona/L=Scottsdale/O=Starfield Technologies, Inc./CN=Starfield Services Root Certificate Authority - G2
        // 3 s:/C=US/ST=Arizona/L=Scottsdale/O=Starfield Technologies, Inc./CN=Starfield Services Root Certificate Authority - G2
        // i:/C=US/O=Starfield Technologies, Inc./OU=Starfield Class 2 Certification Authority
        // ---

        // ie1.realmlab.net (!!!! EXPIRED on May 3, 2018)
        String pem_depth0 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEWDCCA0CgAwIBAgIQBE6+74j1z/Z88OEsSc3VIzANBgkqhkiG9w0BAQsFADBG\n" +
                "MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRUwEwYDVQQLEwxTZXJ2ZXIg\n" +
                "Q0EgMUIxDzANBgNVBAMTBkFtYXpvbjAeFw0xNzA0MDMwMDAwMDBaFw0xODA1MDMx\n" +
                "MjAwMDBaMB0xGzAZBgNVBAMMEiouaWUxLnJlYWxtbGFiLm5ldDCCASIwDQYJKoZI\n" +
                "hvcNAQEBBQADggEPADCCAQoCggEBAKfV/38WJ47qvr4Onopu+XKYlTyTsvouX2VQ\n" +
                "jRopM0gdXehp9BfwnFme8KUVZLSYh0vdmY7Wm5A7oxcL4ZuUpDSs9+xuERNg1YMD\n" +
                "gI46ehj08+KUSfuqsVuw3gpNM6VPtpKY2I4//fJFmJKTWXA/fl35By0Xbuv4I180\n" +
                "FFWu7CV0N4b/QQsjT0+CVvAjHRMMTpw0qtcZGQ4lWNNiqcqUql+Eklm/90S+lyBD\n" +
                "q8YQUwcxhMgxKt6M5zwJpWuIbjov9kygDzlw/YU8P5wqvgocfnnXaKw+rr7EdiTS\n" +
                "U2ZT99JO0F0CPzPZnphNrRtjkJ4Chtp0FVRqAdthpGH4i1VIKP0CAwEAAaOCAWkw\n" +
                "ggFlMB8GA1UdIwQYMBaAFFmkZgZSoHuVkjyjlAcnlnRb+T3QMB0GA1UdDgQWBBRP\n" +
                "5MQbQpMCFJgjiFgEtZUIiKdNeDAdBgNVHREEFjAUghIqLmllMS5yZWFsbWxhYi5u\n" +
                "ZXQwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcD\n" +
                "AjA7BgNVHR8ENDAyMDCgLqAshipodHRwOi8vY3JsLnNjYTFiLmFtYXpvbnRydXN0\n" +
                "LmNvbS9zY2ExYi5jcmwwEwYDVR0gBAwwCjAIBgZngQwBAgEwdQYIKwYBBQUHAQEE\n" +
                "aTBnMC0GCCsGAQUFBzABhiFodHRwOi8vb2NzcC5zY2ExYi5hbWF6b250cnVzdC5j\n" +
                "b20wNgYIKwYBBQUHMAKGKmh0dHA6Ly9jcnQuc2NhMWIuYW1hem9udHJ1c3QuY29t\n" +
                "L3NjYTFiLmNydDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQAObbVL\n" +
                "zDqqFO4iDjR4VRTYQbb3gSDxySqFqMm4iBJBmqgNRDsNDb75EmlbB0udbZ6+LHDK\n" +
                "pmPh81ocdJECHZctidDh1zCkVf3uOYyPJqxNpt0ZCurGMTi4i5kaIbAwR50lZU2V\n" +
                "eSkR5rYFoBIVcUNbXzzOMLTcJrRqbVYz7z9zCN71l12dKNMXdu9tLcec+WCGi0R+\n" +
                "MNBOQ/XVlAzymsmQM6nWb0DEQ86ya9AAAMVQBVgyeEPZNPidxc82kU8pML9mO0Yl\n" +
                "MtbgZWXH1kTppsi+/WbOwy+kalpiMJ7TXIvHmQat81FWiJNTnKwfVEsz79Op8EAW\n" +
                "p9RkpzfSQpZQ30/u\n" +
                "-----END CERTIFICATE-----\n";

        // OU=Server CA 1B/CN=Amazon
        String pem_depth1 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIESTCCAzGgAwIBAgITBn+UV4WH6Kx33rJTMlu8mYtWDTANBgkqhkiG9w0BAQsF\n" +
                "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
                "b24gUm9vdCBDQSAxMB4XDTE1MTAyMjAwMDAwMFoXDTI1MTAxOTAwMDAwMFowRjEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEVMBMGA1UECxMMU2VydmVyIENB\n" +
                "IDFCMQ8wDQYDVQQDEwZBbWF6b24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
                "AoIBAQDCThZn3c68asg3Wuw6MLAd5tES6BIoSMzoKcG5blPVo+sDORrMd4f2AbnZ\n" +
                "cMzPa43j4wNxhplty6aUKk4T1qe9BOwKFjwK6zmxxLVYo7bHViXsPlJ6qOMpFge5\n" +
                "blDP+18x+B26A0piiQOuPkfyDyeR4xQghfj66Yo19V+emU3nazfvpFA+ROz6WoVm\n" +
                "B5x+F2pV8xeKNR7u6azDdU5YVX1TawprmxRC1+WsAYmz6qP+z8ArDITC2FMVy2fw\n" +
                "0IjKOtEXc/VfmtTFch5+AfGYMGMqqvJ6LcXiAhqG5TI+Dr0RtM88k+8XUBCeQ8IG\n" +
                "KuANaL7TiItKZYxK1MMuTJtV9IblAgMBAAGjggE7MIIBNzASBgNVHRMBAf8ECDAG\n" +
                "AQH/AgEAMA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUWaRmBlKge5WSPKOUByeW\n" +
                "dFv5PdAwHwYDVR0jBBgwFoAUhBjMhTTsvAyUlC4IWZzHshBOCggwewYIKwYBBQUH\n" +
                "AQEEbzBtMC8GCCsGAQUFBzABhiNodHRwOi8vb2NzcC5yb290Y2ExLmFtYXpvbnRy\n" +
                "dXN0LmNvbTA6BggrBgEFBQcwAoYuaHR0cDovL2NydC5yb290Y2ExLmFtYXpvbnRy\n" +
                "dXN0LmNvbS9yb290Y2ExLmNlcjA/BgNVHR8EODA2MDSgMqAwhi5odHRwOi8vY3Js\n" +
                "LnJvb3RjYTEuYW1hem9udHJ1c3QuY29tL3Jvb3RjYTEuY3JsMBMGA1UdIAQMMAow\n" +
                "CAYGZ4EMAQIBMA0GCSqGSIb3DQEBCwUAA4IBAQCFkr41u3nPo4FCHOTjY3NTOVI1\n" +
                "59Gt/a6ZiqyJEi+752+a1U5y6iAwYfmXss2lJwJFqMp2PphKg5625kXg8kP2CN5t\n" +
                "6G7bMQcT8C8xDZNtYTd7WPD8UZiRKAJPBXa30/AbwuZe0GaFEQ8ugcYQgSn+IGBI\n" +
                "8/LwhBNTZTUVEWuCUUBVV18YtbAiPq3yXqMB48Oz+ctBWuZSkbvkNodPLamkB2g1\n" +
                "upRyzQ7qDn1X8nn8N8V7YJ6y68AtkHcNSRAnpTitxBKjtKPISLMVCx7i4hncxHZS\n" +
                "yLyKQXhw2W2Xs0qLeC1etA+jTGDK4UfLeC0SF7FSi8o5LL21L8IzApar2pR/\n" +
                "-----END CERTIFICATE-----\n";
        // Amazon Root CA 1
        String pem_depth2 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEkjCCA3qgAwIBAgITBn+USionzfP6wq4rAfkI7rnExjANBgkqhkiG9w0BAQsF\n" +
                "ADCBmDELMAkGA1UEBhMCVVMxEDAOBgNVBAgTB0FyaXpvbmExEzARBgNVBAcTClNj\n" +
                "b3R0c2RhbGUxJTAjBgNVBAoTHFN0YXJmaWVsZCBUZWNobm9sb2dpZXMsIEluYy4x\n" +
                "OzA5BgNVBAMTMlN0YXJmaWVsZCBTZXJ2aWNlcyBSb290IENlcnRpZmljYXRlIEF1\n" +
                "dGhvcml0eSAtIEcyMB4XDTE1MDUyNTEyMDAwMFoXDTM3MTIzMTAxMDAwMFowOTEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
                "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
                "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
                "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
                "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
                "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
                "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
                "jgSubJrIqg0CAwEAAaOCATEwggEtMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/\n" +
                "BAQDAgGGMB0GA1UdDgQWBBSEGMyFNOy8DJSULghZnMeyEE4KCDAfBgNVHSMEGDAW\n" +
                "gBScXwDfqgHXMCs4iKK4bUqc8hGRgzB4BggrBgEFBQcBAQRsMGowLgYIKwYBBQUH\n" +
                "MAGGImh0dHA6Ly9vY3NwLnJvb3RnMi5hbWF6b250cnVzdC5jb20wOAYIKwYBBQUH\n" +
                "MAKGLGh0dHA6Ly9jcnQucm9vdGcyLmFtYXpvbnRydXN0LmNvbS9yb290ZzIuY2Vy\n" +
                "MD0GA1UdHwQ2MDQwMqAwoC6GLGh0dHA6Ly9jcmwucm9vdGcyLmFtYXpvbnRydXN0\n" +
                "LmNvbS9yb290ZzIuY3JsMBEGA1UdIAQKMAgwBgYEVR0gADANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEAYjdCXLwQtT6LLOkMm2xF4gcAevnFWAu5CIw+7bMlPLVvUOTNNWqnkzSW\n" +
                "MiGpSESrnO09tKpzbeR/FoCJbM8oAxiDR3mjEH4wW6w7sGDgd9QIpuEdfF7Au/ma\n" +
                "eyKdpwAJfqxGF4PcnCZXmTA5YpaP7dreqsXMGz7KQ2hsVxa81Q4gLv7/wmpdLqBK\n" +
                "bRRYh5TmOTFffHPLkIhqhBGWJ6bt2YFGpn6jcgAKUj6DiAdjd4lpFw85hdKrCEVN\n" +
                "0FE6/V1dN2RMfjCyVSRCnTawXZwXgWHxyvkQAiSr6w10kY17RSlQOYiypok1JR4U\n" +
                "akcjMS9cmvqtmg5iUaQqqcT5NJ0hGA==\n" +
                "-----END CERTIFICATE-----\n";

        // O=Starfield Technologies, Inc./CN=Starfield Services Root Certificate Authority - G2
        String pem_depth3 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEdTCCA12gAwIBAgIJAKcOSkw0grd/MA0GCSqGSIb3DQEBCwUAMGgxCzAJBgNV\n" +
                "BAYTAlVTMSUwIwYDVQQKExxTdGFyZmllbGQgVGVjaG5vbG9naWVzLCBJbmMuMTIw\n" +
                "MAYDVQQLEylTdGFyZmllbGQgQ2xhc3MgMiBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0\n" +
                "eTAeFw0wOTA5MDIwMDAwMDBaFw0zNDA2MjgxNzM5MTZaMIGYMQswCQYDVQQGEwJV\n" +
                "UzEQMA4GA1UECBMHQXJpem9uYTETMBEGA1UEBxMKU2NvdHRzZGFsZTElMCMGA1UE\n" +
                "ChMcU3RhcmZpZWxkIFRlY2hub2xvZ2llcywgSW5jLjE7MDkGA1UEAxMyU3RhcmZp\n" +
                "ZWxkIFNlcnZpY2VzIFJvb3QgQ2VydGlmaWNhdGUgQXV0aG9yaXR5IC0gRzIwggEi\n" +
                "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDVDDrEKvlO4vW+GZdfjohTsR8/\n" +
                "y8+fIBNtKTrID30892t2OGPZNmCom15cAICyL1l/9of5JUOG52kbUpqQ4XHj2C0N\n" +
                "Tm/2yEnZtvMaVq4rtnQU68/7JuMauh2WLmo7WJSJR1b/JaCTcFOD2oR0FMNnngRo\n" +
                "Ot+OQFodSk7PQ5E751bWAHDLUu57fa4657wx+UX2wmDPE1kCK4DMNEffud6QZW0C\n" +
                "zyyRpqbn3oUYSXxmTqM6bam17jQuug0DuDPfR+uxa40l2ZvOgdFFRjKWcIfeAg5J\n" +
                "Q4W2bHO7ZOphQazJ1FTfhy/HIrImzJ9ZVGif/L4qL8RVHHVAYBeFAlU5i38FAgMB\n" +
                "AAGjgfAwge0wDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0O\n" +
                "BBYEFJxfAN+qAdcwKziIorhtSpzyEZGDMB8GA1UdIwQYMBaAFL9ft9HO3R+G9FtV\n" +
                "rNzXEMIOqYjnME8GCCsGAQUFBwEBBEMwQTAcBggrBgEFBQcwAYYQaHR0cDovL28u\n" +
                "c3MyLnVzLzAhBggrBgEFBQcwAoYVaHR0cDovL3guc3MyLnVzL3guY2VyMCYGA1Ud\n" +
                "HwQfMB0wG6AZoBeGFWh0dHA6Ly9zLnNzMi51cy9yLmNybDARBgNVHSAECjAIMAYG\n" +
                "BFUdIAAwDQYJKoZIhvcNAQELBQADggEBACMd44pXyn3pF3lM8R5V/cxTbj5HD9/G\n" +
                "VfKyBDbtgB9TxF00KGu+x1X8Z+rLP3+QsjPNG1gQggL4+C/1E2DUBc7xgQjB3ad1\n" +
                "l08YuW3e95ORCLp+QCztweq7dp4zBncdDQh/U90bZKuCJ/Fp1U1ervShw3WnWEQt\n" +
                "8jxwmKy6abaVd38PMV4s/KCHOkdp8Hlf9BRUpJVeEXgSYCfOn8J3/yNTd126/+pZ\n" +
                "59vPr5KW7ySaNRB6nJHGDn2Z9j8Z3/VyVOEVqQdZe4O/Ui5GjLIAZHYcSNPYeehu\n" +
                "VsyuLAOQ1xk4meTKCRlb/weWsKh/NEnfVqn3sF/tM+2MR7cwA130A4w=\n" +
                "-----END CERTIFICATE-----\n";

        String serverAddress = "nabil-test.ie1.realmlab.net";

        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth3, 3));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth2, 2));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth1, 1));
        assertFalse(SyncManager.sslVerifyCallback(serverAddress, pem_depth0, 0));
    }

    @Test
    public void sslVerifyCallback_shouldVerifyHostname() {
        // simulating the following certificate chain

        // 0 s:/CN=us1a.cloud.realm.io
        //   i:/C=US/O=Amazon/OU=Server CA 1B/CN=Amazon
        String pem_depth0 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEfjCCA2agAwIBAgIQAuZyKHDOzYP160MtNtRBEjANBgkqhkiG9w0BAQsFADBG\n" +
                "MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRUwEwYDVQQLEwxTZXJ2ZXIg\n" +
                "Q0EgMUIxDzANBgNVBAMTBkFtYXpvbjAeFw0xODAyMTkwMDAwMDBaFw0xOTAzMTkx\n" +
                "MjAwMDBaMB4xHDAaBgNVBAMTE3VzMWEuY2xvdWQucmVhbG0uaW8wggEiMA0GCSqG\n" +
                "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQC6XER+3bFiK4TCc5lQv/O3xTc9oC/bcPVr\n" +
                "zs52mzcGW/wNH6dxW3i3T3gz3Pit8TDkDf0tzoZNdfr7PYs+BPtinM3ZbKSSnF6G\n" +
                "5F8HNpe/1p1blko22wJDa9OyZD4tZ3f6hBlUU+8tHFC2B7BGEzuVKf3Aacap0wdh\n" +
                "KsAAaF/mbtLQaelRFtHcIOz2B28e7Fub/iwJGCW79Keq+lDRLG+xayEsBqO3+FJ3\n" +
                "h4FxbhsKW/O5tb/5B4dZfgJopWZfcmTUZ89ZX2IYaukfwkrV+/09ZAr87jMi9E7+\n" +
                "zU37qHtrWVWQV48BxdWiMmmvJb0ytYM0rxal2YuXi6NOBTP0sbxVAgMBAAGjggGO\n" +
                "MIIBijAfBgNVHSMEGDAWgBRZpGYGUqB7lZI8o5QHJ5Z0W/k90DAdBgNVHQ4EFgQU\n" +
                "ZNEE3UPcZg2ZOJd4eMZryxUTvKswNQYDVR0RBC4wLIITdXMxYS5jbG91ZC5yZWFs\n" +
                "bS5pb4IVKi51czFhLmNsb3VkLnJlYWxtLmlvMA4GA1UdDwEB/wQEAwIFoDAdBgNV\n" +
                "HSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwOwYDVR0fBDQwMjAwoC6gLIYqaHR0\n" +
                "cDovL2NybC5zY2ExYi5hbWF6b250cnVzdC5jb20vc2NhMWIuY3JsMCAGA1UdIAQZ\n" +
                "MBcwCwYJYIZIAYb9bAECMAgGBmeBDAECATB1BggrBgEFBQcBAQRpMGcwLQYIKwYB\n" +
                "BQUHMAGGIWh0dHA6Ly9vY3NwLnNjYTFiLmFtYXpvbnRydXN0LmNvbTA2BggrBgEF\n" +
                "BQcwAoYqaHR0cDovL2NydC5zY2ExYi5hbWF6b250cnVzdC5jb20vc2NhMWIuY3J0\n" +
                "MAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQELBQADggEBAAserhwXWohdFjImCcCh\n" +
                "0XGW7s47vygasV4kE7vg59dz5RQrVuu+U0HFKTuPw6d4xSaQrUq1wo76RJtZalpG\n" +
                "ek9vOvS0GWxjSsts2D0oWZXq772bhlXRfj21NsgwzfWMXIrUaV32l5qDhin1wx7x\n" +
                "oZL7mNQ75qFB56jv5zzsX2woFv1GN0a03nFgy9Jk6aWCM5Q3oujrxJJWsgXIMloj\n" +
                "uqg+I4MfhTEC1ZnGOEoO4Rq3i1rSLa59mv4lhcO/+yrEENKESgx8/8DnIjQoEuRp\n" +
                "QtbxCVxPYfnjBuRuvyTfSo1GMK6SuhvkqVbDhBbRDDCh2T8Nmea3BcFi1kcpImOr\n" +
                "MI4=\n" +
                "-----END CERTIFICATE-----";

        //  1 s:/C=US/O=Amazon/OU=Server CA 1B/CN=Amazon
        //  i:/C=US/O=Amazon/CN=Amazon Root CA 1
        String pem_depth1 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIESTCCAzGgAwIBAgITBn+UV4WH6Kx33rJTMlu8mYtWDTANBgkqhkiG9w0BAQsF\n" +
                "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
                "b24gUm9vdCBDQSAxMB4XDTE1MTAyMjAwMDAwMFoXDTI1MTAxOTAwMDAwMFowRjEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEVMBMGA1UECxMMU2VydmVyIENB\n" +
                "IDFCMQ8wDQYDVQQDEwZBbWF6b24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
                "AoIBAQDCThZn3c68asg3Wuw6MLAd5tES6BIoSMzoKcG5blPVo+sDORrMd4f2AbnZ\n" +
                "cMzPa43j4wNxhplty6aUKk4T1qe9BOwKFjwK6zmxxLVYo7bHViXsPlJ6qOMpFge5\n" +
                "blDP+18x+B26A0piiQOuPkfyDyeR4xQghfj66Yo19V+emU3nazfvpFA+ROz6WoVm\n" +
                "B5x+F2pV8xeKNR7u6azDdU5YVX1TawprmxRC1+WsAYmz6qP+z8ArDITC2FMVy2fw\n" +
                "0IjKOtEXc/VfmtTFch5+AfGYMGMqqvJ6LcXiAhqG5TI+Dr0RtM88k+8XUBCeQ8IG\n" +
                "KuANaL7TiItKZYxK1MMuTJtV9IblAgMBAAGjggE7MIIBNzASBgNVHRMBAf8ECDAG\n" +
                "AQH/AgEAMA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUWaRmBlKge5WSPKOUByeW\n" +
                "dFv5PdAwHwYDVR0jBBgwFoAUhBjMhTTsvAyUlC4IWZzHshBOCggwewYIKwYBBQUH\n" +
                "AQEEbzBtMC8GCCsGAQUFBzABhiNodHRwOi8vb2NzcC5yb290Y2ExLmFtYXpvbnRy\n" +
                "dXN0LmNvbTA6BggrBgEFBQcwAoYuaHR0cDovL2NydC5yb290Y2ExLmFtYXpvbnRy\n" +
                "dXN0LmNvbS9yb290Y2ExLmNlcjA/BgNVHR8EODA2MDSgMqAwhi5odHRwOi8vY3Js\n" +
                "LnJvb3RjYTEuYW1hem9udHJ1c3QuY29tL3Jvb3RjYTEuY3JsMBMGA1UdIAQMMAow\n" +
                "CAYGZ4EMAQIBMA0GCSqGSIb3DQEBCwUAA4IBAQCFkr41u3nPo4FCHOTjY3NTOVI1\n" +
                "59Gt/a6ZiqyJEi+752+a1U5y6iAwYfmXss2lJwJFqMp2PphKg5625kXg8kP2CN5t\n" +
                "6G7bMQcT8C8xDZNtYTd7WPD8UZiRKAJPBXa30/AbwuZe0GaFEQ8ugcYQgSn+IGBI\n" +
                "8/LwhBNTZTUVEWuCUUBVV18YtbAiPq3yXqMB48Oz+ctBWuZSkbvkNodPLamkB2g1\n" +
                "upRyzQ7qDn1X8nn8N8V7YJ6y68AtkHcNSRAnpTitxBKjtKPISLMVCx7i4hncxHZS\n" +
                "yLyKQXhw2W2Xs0qLeC1etA+jTGDK4UfLeC0SF7FSi8o5LL21L8IzApar2pR/\n" +
                "-----END CERTIFICATE-----";

        //  2 s:/C=US/O=Amazon/CN=Amazon Root CA 1
        //   i:/C=US/ST=Arizona/L=Scottsdale/O=Starfield Technologies, Inc./CN=Starfield Services Root Certificate Authority - G2
        String pem_depth2 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEkjCCA3qgAwIBAgITBn+USionzfP6wq4rAfkI7rnExjANBgkqhkiG9w0BAQsF\n" +
                "ADCBmDELMAkGA1UEBhMCVVMxEDAOBgNVBAgTB0FyaXpvbmExEzARBgNVBAcTClNj\n" +
                "b3R0c2RhbGUxJTAjBgNVBAoTHFN0YXJmaWVsZCBUZWNobm9sb2dpZXMsIEluYy4x\n" +
                "OzA5BgNVBAMTMlN0YXJmaWVsZCBTZXJ2aWNlcyBSb290IENlcnRpZmljYXRlIEF1\n" +
                "dGhvcml0eSAtIEcyMB4XDTE1MDUyNTEyMDAwMFoXDTM3MTIzMTAxMDAwMFowOTEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
                "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
                "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
                "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
                "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
                "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
                "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
                "jgSubJrIqg0CAwEAAaOCATEwggEtMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/\n" +
                "BAQDAgGGMB0GA1UdDgQWBBSEGMyFNOy8DJSULghZnMeyEE4KCDAfBgNVHSMEGDAW\n" +
                "gBScXwDfqgHXMCs4iKK4bUqc8hGRgzB4BggrBgEFBQcBAQRsMGowLgYIKwYBBQUH\n" +
                "MAGGImh0dHA6Ly9vY3NwLnJvb3RnMi5hbWF6b250cnVzdC5jb20wOAYIKwYBBQUH\n" +
                "MAKGLGh0dHA6Ly9jcnQucm9vdGcyLmFtYXpvbnRydXN0LmNvbS9yb290ZzIuY2Vy\n" +
                "MD0GA1UdHwQ2MDQwMqAwoC6GLGh0dHA6Ly9jcmwucm9vdGcyLmFtYXpvbnRydXN0\n" +
                "LmNvbS9yb290ZzIuY3JsMBEGA1UdIAQKMAgwBgYEVR0gADANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEAYjdCXLwQtT6LLOkMm2xF4gcAevnFWAu5CIw+7bMlPLVvUOTNNWqnkzSW\n" +
                "MiGpSESrnO09tKpzbeR/FoCJbM8oAxiDR3mjEH4wW6w7sGDgd9QIpuEdfF7Au/ma\n" +
                "eyKdpwAJfqxGF4PcnCZXmTA5YpaP7dreqsXMGz7KQ2hsVxa81Q4gLv7/wmpdLqBK\n" +
                "bRRYh5TmOTFffHPLkIhqhBGWJ6bt2YFGpn6jcgAKUj6DiAdjd4lpFw85hdKrCEVN\n" +
                "0FE6/V1dN2RMfjCyVSRCnTawXZwXgWHxyvkQAiSr6w10kY17RSlQOYiypok1JR4U\n" +
                "akcjMS9cmvqtmg5iUaQqqcT5NJ0hGA==\n" +
                "-----END CERTIFICATE-----";

        //  3 s:/C=US/ST=Arizona/L=Scottsdale/O=Starfield Technologies, Inc./CN=Starfield Services Root Certificate Authority - G2
        //   i:/C=US/O=Starfield Technologies, Inc./OU=Starfield Class 2 Certification Authority
        String pem_depth3 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEdTCCA12gAwIBAgIJAKcOSkw0grd/MA0GCSqGSIb3DQEBCwUAMGgxCzAJBgNV\n" +
                "BAYTAlVTMSUwIwYDVQQKExxTdGFyZmllbGQgVGVjaG5vbG9naWVzLCBJbmMuMTIw\n" +
                "MAYDVQQLEylTdGFyZmllbGQgQ2xhc3MgMiBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0\n" +
                "eTAeFw0wOTA5MDIwMDAwMDBaFw0zNDA2MjgxNzM5MTZaMIGYMQswCQYDVQQGEwJV\n" +
                "UzEQMA4GA1UECBMHQXJpem9uYTETMBEGA1UEBxMKU2NvdHRzZGFsZTElMCMGA1UE\n" +
                "ChMcU3RhcmZpZWxkIFRlY2hub2xvZ2llcywgSW5jLjE7MDkGA1UEAxMyU3RhcmZp\n" +
                "ZWxkIFNlcnZpY2VzIFJvb3QgQ2VydGlmaWNhdGUgQXV0aG9yaXR5IC0gRzIwggEi\n" +
                "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDVDDrEKvlO4vW+GZdfjohTsR8/\n" +
                "y8+fIBNtKTrID30892t2OGPZNmCom15cAICyL1l/9of5JUOG52kbUpqQ4XHj2C0N\n" +
                "Tm/2yEnZtvMaVq4rtnQU68/7JuMauh2WLmo7WJSJR1b/JaCTcFOD2oR0FMNnngRo\n" +
                "Ot+OQFodSk7PQ5E751bWAHDLUu57fa4657wx+UX2wmDPE1kCK4DMNEffud6QZW0C\n" +
                "zyyRpqbn3oUYSXxmTqM6bam17jQuug0DuDPfR+uxa40l2ZvOgdFFRjKWcIfeAg5J\n" +
                "Q4W2bHO7ZOphQazJ1FTfhy/HIrImzJ9ZVGif/L4qL8RVHHVAYBeFAlU5i38FAgMB\n" +
                "AAGjgfAwge0wDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0O\n" +
                "BBYEFJxfAN+qAdcwKziIorhtSpzyEZGDMB8GA1UdIwQYMBaAFL9ft9HO3R+G9FtV\n" +
                "rNzXEMIOqYjnME8GCCsGAQUFBwEBBEMwQTAcBggrBgEFBQcwAYYQaHR0cDovL28u\n" +
                "c3MyLnVzLzAhBggrBgEFBQcwAoYVaHR0cDovL3guc3MyLnVzL3guY2VyMCYGA1Ud\n" +
                "HwQfMB0wG6AZoBeGFWh0dHA6Ly9zLnNzMi51cy9yLmNybDARBgNVHSAECjAIMAYG\n" +
                "BFUdIAAwDQYJKoZIhvcNAQELBQADggEBACMd44pXyn3pF3lM8R5V/cxTbj5HD9/G\n" +
                "VfKyBDbtgB9TxF00KGu+x1X8Z+rLP3+QsjPNG1gQggL4+C/1E2DUBc7xgQjB3ad1\n" +
                "l08YuW3e95ORCLp+QCztweq7dp4zBncdDQh/U90bZKuCJ/Fp1U1ervShw3WnWEQt\n" +
                "8jxwmKy6abaVd38PMV4s/KCHOkdp8Hlf9BRUpJVeEXgSYCfOn8J3/yNTd126/+pZ\n" +
                "59vPr5KW7ySaNRB6nJHGDn2Z9j8Z3/VyVOEVqQdZe4O/Ui5GjLIAZHYcSNPYeehu\n" +
                "VsyuLAOQ1xk4meTKCRlb/weWsKh/NEnfVqn3sF/tM+2MR7cwA130A4w=\n" +
                "-----END CERTIFICATE-----";

        String serverAddress = "foo.us1a.cloud.realm.io";

        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth3, 3));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth2, 2));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth1, 1));
        assertTrue(SyncManager.sslVerifyCallback(serverAddress, pem_depth0, 0));

        // reaching depth0 will validate (or not) the entire chain, then removing the PEMs from memory
        // make sure the hostname verify works

        String wrongServerAddress = "hax0r-us1a.cloud2.realm.io";
        assertTrue(SyncManager.sslVerifyCallback(wrongServerAddress, pem_depth3, 3));
        assertTrue(SyncManager.sslVerifyCallback(wrongServerAddress, pem_depth2, 2));
        assertTrue(SyncManager.sslVerifyCallback(wrongServerAddress, pem_depth1, 1));
        // the method fails because of the hostname verification
        assertFalse(SyncManager.sslVerifyCallback(wrongServerAddress, pem_depth0, 0));
    }
}
