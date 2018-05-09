package br.gunna.pagarmeandroid;

import android.text.TextUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gunna.pagarmeandroid.pagarme.PagarMeAndroid;
import br.gunna.pagarmeandroid.pagarme.PagarMeRequest;
import br.gunna.pagarmeandroid.pagarme.PagarMeResponse;
import br.gunna.pagarmeandroid.pagarme.exception.EmptyFieldException;
import br.gunna.pagarmeandroid.pagarme.exception.InitializationException;
import br.gunna.pagarmeandroid.pagarme.exception.InvalidKeyException;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class PagarMeAndroidUnitTest {



    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }


    @Test(expected = InvalidKeyException.class)
    public void check_empty_api_key_test() throws Exception{
        PagarMeAndroid.initialize("");
    }

    @Test(expected = InitializationException.class)
    public void check_initialization_test() throws Exception{
        PagarMeAndroid.getsInstance();
    }

    @Test
    public void check_instance_is_not_null(){

    }

}