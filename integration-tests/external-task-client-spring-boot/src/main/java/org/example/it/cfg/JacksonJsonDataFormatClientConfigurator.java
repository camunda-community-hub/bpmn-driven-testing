package org.example.it.cfg;

import org.camunda.bpm.client.spi.DataFormatConfigurator;
import org.camunda.bpm.client.variable.impl.format.json.JacksonJsonDataFormat;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonJsonDataFormatClientConfigurator implements DataFormatConfigurator<JacksonJsonDataFormat> {

  @Override
  public Class<JacksonJsonDataFormat> getDataFormatClass() {
    return JacksonJsonDataFormat.class;
  }

  @Override
  public void configure(JacksonJsonDataFormat dataFormat) {
    dataFormat.getObjectMapper().registerModule(new JavaTimeModule());
  }
}
