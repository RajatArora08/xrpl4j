package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransaction;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.io.IOException;

/**
 * Custom Jackson Deserializer for {@link AccountTransactionsTransaction}s. This is necessary because Jackson
 * does not deserialize {@link com.fasterxml.jackson.annotation.JsonUnwrapped} fields intelligently.
 */
public class AccountTransactionsTransactionDeserializer extends StdDeserializer<AccountTransactionsTransaction<?>> {

  /**
   * No-args constructor.
   */
  public AccountTransactionsTransactionDeserializer() {
    super(AccountTransactionsTransaction.class);
  }

  @Override
  public AccountTransactionsTransaction<?> deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = objectMapper.readTree(jsonParser);

    Transaction transaction = objectMapper.readValue(node.toString(), Transaction.class);
    long ledgerIndex = node.get("ledger_index").asLong(-1L);
    String hash = node.get("hash").asText();
    return AccountTransactionsTransaction.builder()
      .transaction(transaction)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(ledgerIndex)))
      .hash(Hash256.of(hash))
      .build();
  }
}
