# [PagarMe-Android](https://danielgunna.github.io/PagarMe-Android/)

[![Release](https://jitpack.io/v/danielgunna/PagarMe-Android.svg)](https://jitpack.io/#danielgunna/PagarMe-Android)

Uma biblioteca simples para Android usada para gerar card hash de cartões de crédito para uso no gateway de pagamento [Pagar.me](https://pagar.me/)


# Configuração 
*A API do Android mínima para esse projeto é a 16.* 

Para configurar basta adicionar a depêndencia no arquivo de configuração gradle:

```groovy
dependencies {
	  compile 'com.github.danielgunna:PagarMe-Android:0.0.1'
          //usando Gradle 3.+
          implementation 'com.github.danielgunna:PagarMe-Android:0.0.1'
}
```
# Como usar ?

Para gerar um card hash para um cartão basta fazer como o trecho abaixo:

```java
PagarMeAndroid.initialize("[YOUR_PAGARME_KEY]");

PagarMeAndroid.getsInstance()
     .cvv("123") 
     .expirationDate("12/25")
     .holderName("Daniel Gunna")
     .number("411111111111")
     .generateCardHash(new PagarMeAndroid.PagarMeListener() {
         @Override
         public void onSuccess(PagarMeRequest pagarMeRequest, 
                             PagarMeResponse pagarMeResponse, String s) {
            Log.d("CardHash generated : ", s);
         }

         @Override
         public void onError(Exception e) {
             //handle erros 
         }
});
```



