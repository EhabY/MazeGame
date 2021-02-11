package serialization;

public interface JsonEncodable {
    String encodeUsing(Encoder encoder);
}
