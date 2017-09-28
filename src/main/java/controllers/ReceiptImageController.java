package controllers;

import api.ReceiptSuggestionResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.hibernate.validator.constraints.NotEmpty;


@Path("/images")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptImageController {
    private final AnnotateImageRequest.Builder requestBuilder;

    public ReceiptImageController() {
        // DOCUMENT_TEXT_DETECTION is not the best or only OCR method available
        Feature ocrFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        this.requestBuilder = AnnotateImageRequest.newBuilder().addFeatures(ocrFeature);

    }

    /**
     * This borrows heavily from the Google Vision API Docs.  See:
     * https://cloud.google.com/vision/docs/detecting-fulltext
     * <p>
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     * <p>
     * public class ReceiptSuggestionResponse {
     * String merchantName;
     * String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        AnnotateImageRequest request = this.requestBuilder.setImage(img).build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse responses = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = responses.getResponses(0);
            List<EntityAnnotation> annotationList = res.getTextAnnotationsList();

            String merchantName = null;
            BigDecimal amount = null;

            EntityAnnotation topMost = null;
            EntityAnnotation bottomMost = null;

            EntityAnnotation fullPhrase = null;

            List<EntityAnnotation> wordList;

            if (annotationList.size() > 0) {
                fullPhrase = annotationList.get(0);
                wordList = annotationList.subList(1, annotationList.size());
            } else {
                return new ReceiptSuggestionResponse(null, null);
            }

            for (EntityAnnotation word : wordList) {
                Integer placement = word.getBoundingPoly().getVertices(0).getY();

                if (bottomMost == null || topMost == null) {
                    bottomMost = word;
                    topMost = word;
                }

                if (placement >= bottomMost.getBoundingPoly().getVertices(0).getY()) {
                    try {
                        amount = new BigDecimal(word.getDescription());
                        bottomMost = word;
                    } catch (Exception err) {
                    }
                }

                if (
                        placement <= topMost.getBoundingPoly().getVertices(0).getY() &&
                                (merchantName == null || word.getDescription().split("\n").length <= merchantName.split("\n").length)
                        ) {
                    topMost = word;
                    merchantName = word.getDescription();
                }
            }

            if (merchantName == null) {
                merchantName = fullPhrase.getDescription();
            }

            return new ReceiptSuggestionResponse(merchantName, amount);
        }
    }
}
