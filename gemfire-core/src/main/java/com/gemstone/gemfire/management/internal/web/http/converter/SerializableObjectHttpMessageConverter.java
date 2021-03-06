/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.management.internal.web.http.converter;

import java.io.IOException;
import java.io.Serializable;

import com.gemstone.gemfire.internal.lang.ObjectUtils;
import com.gemstone.gemfire.internal.util.IOUtils;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

/**
 * The SerializableObjectHttpMessageConverter class is a Spring HttpMessageConverter for converting bytes streams to/from
 * Serializable Objects.
 * <p/>
 * @author John Blum
 * @see java.io.Serializable
 * @see org.springframework.http.HttpInputMessage
 * @see org.springframework.http.HttpMessage
 * @see org.springframework.http.HttpOutputMessage
 * @see org.springframework.http.MediaType
 * @see org.springframework.http.converter.AbstractHttpMessageConverter
 * @since 8.0
 */
@SuppressWarnings("unused")
public class SerializableObjectHttpMessageConverter extends AbstractHttpMessageConverter<Serializable> {

  public SerializableObjectHttpMessageConverter() {
    super(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL);
  }

  /*
  @Override
  public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
    return canRead(mediaType);
  }
  */

  @Override
  protected boolean supports(final Class<?> type) {
    if (logger.isTraceEnabled()) {
      logger.trace(String.format("%1$s.supports(%2$s)", getClass().getName(), type.getName()),
        new Throwable());
    }
    /*
    logger.warn(String.format("%1$s.supports(%2$s)", getClass().getName(), type.getName()));
    final StringWriter writer = new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(writer));
    logger.warn(writer.toString());
    */
    return (type != null && Serializable.class.isAssignableFrom(type));
  }

  @Override
  protected Serializable readInternal(final Class<? extends Serializable> type, final HttpInputMessage inputMessage)
    throws IOException, HttpMessageNotReadableException
  {
    try {
      return type.cast(IOUtils.deserializeObject(IOUtils.toByteArray(inputMessage.getBody()),
        ObjectUtils.defaultIfNull(type.getClassLoader(), getClass().getClassLoader())));
    }
    catch (ClassNotFoundException e) {
      throw new HttpMessageNotReadableException(String.format(
        "Unable to convert the HTTP message body into an Object of type (%1$s)", type.getName()), e);
    }
  }

  protected void setContentLength(final HttpMessage message, final byte[] messageBody) {
    message.getHeaders().setContentLength(messageBody.length);
  }

  @Override
  protected void writeInternal(final Serializable serializableObject, final HttpOutputMessage outputMessage)
    throws IOException, HttpMessageNotWritableException
  {
    final byte[] messageBody = IOUtils.serializeObject(serializableObject);
    setContentLength(outputMessage, messageBody);
    StreamUtils.copy(messageBody, outputMessage.getBody());
  }

}
