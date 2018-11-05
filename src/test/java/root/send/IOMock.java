/*
 * Copyright 2018-2019 Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package root.send;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class IOMock {

    @Captor
    ArgumentCaptor<Request> requestCaptor;

    @Mock
    OkHttpClient httpClient;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUrl() {
        WebhookClient client = prepare();

        client.send("Hello World");

        verify(httpClient, after(100).only()).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        Assert.assertEquals("POST", req.method());
        Assert.assertEquals(String.format("https://discordapp.com/api/v7/webhooks/%d/%s?wait=false", 1234, "token"), req.url().toString());
    }

    @Test
    public void messageBodyUsed() {
        //implicitly checks json sent due to json (requestbody) being checked in MessageTest testclass
        RequestBody body = new WebhookMessageBuilder()
                .setContent("CONTENT!")
                .setUsername("MrWebhook")
                .setAvatarUrl("linkToImage")
                .setTTS(true)
                .addEmbeds(new WebhookEmbedBuilder().setDescription("embed").build())
                .build().getBody();

        WebhookMessage mock = mock(WebhookMessage.class);
        when(mock.getBody()).thenReturn(body);

        WebhookClient client = prepare();
        client.send(mock);

        verify(httpClient, after(100).only()).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        Assert.assertSame(body, req.body());
    }

    private WebhookClient prepare() {
        when(httpClient.newCall(any())).thenReturn(null);   //will make WebhookClient code throw NPE internally, which we don't care about

        return new WebhookClientBuilder(1234, "token").setWait(false).setHttpClient(httpClient).build();
    }
}
