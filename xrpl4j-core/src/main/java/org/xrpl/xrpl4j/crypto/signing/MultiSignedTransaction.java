package org.xrpl.xrpl4j.crypto.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the bytes for and assembles signers for a multi-signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMultiSignedTransaction.class)
@JsonDeserialize(as = ImmutableMultiSignedTransaction.class)
public interface MultiSignedTransaction<T extends Transaction> extends SignedTransaction<T> {

  /**
   * A builder.
   *
   * @param <T> An instance of {@link Transaction}.
   *
   * @return An {@link ImmutableMultiSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableMultiSignedTransaction.Builder<T> builder() {
    return ImmutableMultiSignedTransaction.builder();
  }

  /**
   * The set of signatures and public keys used to sign.
   *
   * @return A {@link Set} of {@link SignatureWithPublicKey}s.
   */
  Set<SignatureWithPublicKey> signatureWithPublicKeySet();

  /**
   * The transaction with all signers in {@link #signatureWithPublicKeySet()} added to the {@link Transaction#signers()}
   * field in the correct order.
   *
   * @return A {@link T}.
   */
  @Override
  @Value.Derived
  default T signedTransaction() {
    List<SignerWrapper> signers = signatureWithPublicKeySet().stream()
      .map(signatureWithPublicKey ->
        Signer.builder()
          .account(signatureWithPublicKey.signingPublicKey().deriveAddress())
          .signingPublicKey(signatureWithPublicKey.signingPublicKey())
          .transactionSignature(signatureWithPublicKey.transactionSignature().base16Value())
          .build()
      ).map(SignerWrapper::of)
      .sorted(
        Comparator.comparing(
          signature -> new BigInteger(
            AddressCodec.getInstance().decodeAccountId(signature.signer().account()).hexValue(), 16
          )
        )
      )
      .collect(Collectors.toList());

    return SignatureUtils.getInstance().addMultiSignaturesToTransaction(unsignedTransaction(), signers);
  }

}