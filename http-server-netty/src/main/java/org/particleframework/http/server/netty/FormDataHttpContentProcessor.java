/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.http.server.netty;

import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.multipart.*;
import org.particleframework.http.MediaType;
import org.particleframework.http.server.netty.configuration.NettyHttpServerConfiguration;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.charset.Charset;

/**
 * <p>Decodes {@link org.particleframework.http.MediaType#MULTIPART_FORM_DATA} in a non-blocking manner</p>
 *
 * <p>Designed to be used by a single thread</p>
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class FormDataHttpContentProcessor extends AbstractHttpContentProcessor<HttpData> {

    private final HttpPostRequestDecoder decoder;
    private final boolean enabled;

    public FormDataHttpContentProcessor(NettyHttpRequest<?> nettyHttpRequest, NettyHttpServerConfiguration configuration) {
        super(nettyHttpRequest, configuration);
        Charset characterEncoding = nettyHttpRequest.getCharacterEncoding();
        DefaultHttpDataFactory factory = new DefaultHttpDataFactory(configuration.getMultipart().isDisk(), characterEncoding);
        factory.setMaxLimit(configuration.getMultipart().getMaxFileSize());
        this.decoder = new HttpPostRequestDecoder(factory, nettyHttpRequest.getNativeRequest(), characterEncoding);
        this.enabled = nettyHttpRequest.getContentType() == MediaType.APPLICATION_FORM_URLENCODED_TYPE ||
                                configuration.getMultipart().isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    protected void onData(ByteBufHolder message) {
        Subscriber<? super HttpData> subscriber = getSubscriber();

        if(message instanceof HttpContent) {
            try {
                HttpContent httpContent = (HttpContent) message;
                HttpPostRequestDecoder postRequestDecoder = this.decoder;
                postRequestDecoder.offer(httpContent);
                while (postRequestDecoder.hasNext()) {
                    InterfaceHttpData data = postRequestDecoder.next();
                    try {
                        switch (data.getHttpDataType()) {
                            case Attribute:
                                Attribute attribute = (Attribute) data;
                                subscriber.onNext(attribute);
                                break;
                            case FileUpload:
                                FileUpload fileUpload = (FileUpload) data;
                                if (fileUpload.isCompleted()) {
                                    subscriber.onNext(fileUpload);
                                }
                                break;
                        }
                    } finally {
                        data.release();
                    }

                }
                InterfaceHttpData currentPartialHttpData = postRequestDecoder.currentPartialHttpData();
                if(currentPartialHttpData instanceof HttpData) {
                    subscriber.onNext((HttpData) currentPartialHttpData);
                }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
                // ok, ignore
            } catch (Throwable e) {
                onError(e);
            }
        }
        else {
            message.release();
        }
    }

    @Override
    protected void doAfterOnError(Throwable throwable) {
        decoder.destroy();
    }

    @Override
    protected void doAfterComplete() {
        decoder.destroy();
    }

}
