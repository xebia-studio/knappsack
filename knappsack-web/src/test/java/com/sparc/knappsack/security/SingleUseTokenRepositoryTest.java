package com.sparc.knappsack.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * [Class Description]
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleUseTokenRepositoryTest {

    private SingleUseTokenRepository repository;

    @Mock(name = "singleUseToken")
    private final SingleUseToken TOKEN = Mockito.mock(SingleUseToken.class);

    @Mock(name = "singleUseToken")
    private final SingleUseToken TOKEN2 = Mockito.mock(SingleUseToken.class);

    @Before
    public void setUp() {
        repository = new SingleUseTokenRepositoryImpl();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutToken() throws Exception {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        repository.putToken(TOKEN);
        Class exposedRepository = repository.getClass();
        Field tokenMapField = exposedRepository.getDeclaredField("tokens");
        tokenMapField.setAccessible(true);
        Map<String, SingleUseToken> tokenMap = (Map<String, SingleUseToken>) tokenMapField.get(repository);

        assertEquals(TOKEN, tokenMap.get(hash));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateTokenSuccess() throws Exception {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        repository.putToken(TOKEN);
        repository.updateToken(TOKEN);

        Class exposedRepository = repository.getClass();
        Field tokenMapField = exposedRepository.getDeclaredField("tokens");
        tokenMapField.setAccessible(true);
        Map<String, SingleUseToken> tokenMap = (Map<String, SingleUseToken>) tokenMapField.get(repository);
        assertEquals(1, tokenMap.size());
        assertEquals(TOKEN, tokenMap.get(hash));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateTokenNoToken() throws Exception {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        repository.updateToken(TOKEN);

        Class exposedRepository = repository.getClass();
        Field tokenMapField = exposedRepository.getDeclaredField("tokens");
        tokenMapField.setAccessible(true);
        Map<String, SingleUseToken> tokenMap = (Map<String, SingleUseToken>) tokenMapField.get(repository);
        assertEquals(1, tokenMap.size());
        assertEquals(TOKEN, tokenMap.get(hash));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveTokenSuccess() throws Exception {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        String hash2 = "c4ca4238a0b923820dcc509a6f75849c";
        Mockito.when(TOKEN2.getSessionIdHash()).thenReturn(hash2);
        repository.putToken(TOKEN);
        repository.putToken(TOKEN2);
        repository.removeToken(TOKEN.getSessionIdHash());

        Class exposedRepository = repository.getClass();
        Field tokenMapField = exposedRepository.getDeclaredField("tokens");
        tokenMapField.setAccessible(true);
        Map<String, SingleUseToken> tokenMap = (Map<String, SingleUseToken>) tokenMapField.get(repository);
        assertEquals(1, tokenMap.size());
        assertEquals(TOKEN2, tokenMap.get(hash2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveTokenNoToken() throws Exception {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        String hash2 = "c4ca4238a0b923820dcc509a6f75849c";
        Mockito.when(TOKEN2.getSessionIdHash()).thenReturn(hash2);
        repository.putToken(TOKEN);
        repository.removeToken(TOKEN2.getSessionIdHash());

        Class exposedRepository = repository.getClass();
        Field tokenMapField = exposedRepository.getDeclaredField("tokens");
        tokenMapField.setAccessible(true);
        Map<String, SingleUseToken> tokenMap = (Map<String, SingleUseToken>) tokenMapField.get(repository);
        assertEquals(1, tokenMap.size());
        assertEquals(TOKEN, tokenMap.get(hash));
    }

    @Test
    public void testGetTokenSuccess() {
        String hash = "c4ca4238a0b923820dcc509a6f75849b";
        Mockito.when(TOKEN.getSessionIdHash()).thenReturn(hash);
        String hash2 = "c4ca4238a0b923820dcc509a6f75849c";
        Mockito.when(TOKEN2.getSessionIdHash()).thenReturn(hash2);
        repository.putToken(TOKEN);
        repository.putToken(TOKEN2);

        assertEquals(TOKEN, repository.getToken(hash));
        assertEquals(TOKEN2, repository.getToken(hash2));
    }

}


