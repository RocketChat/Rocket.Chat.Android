package chat.rocket.android.model;

public class RocketChatDocument {
    public String id;
    public Class<? extends AbstractModel> docType;
    public String docID;

    private RocketChatDocument(){}
    public RocketChatDocument(String id, Class<? extends AbstractModel> docType, String docID) {
        this.id = id;
        this.docType = docType;
        this.docID = docID;
    }
}
