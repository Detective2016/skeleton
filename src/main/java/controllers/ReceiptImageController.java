package controllers;

import api.ReceiptSuggestionResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import static java.lang.System.out;

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
     *
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     *
     * public class ReceiptSuggestionResponse {
     *     String merchantName;
     *     String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        AnnotateImageRequest request = this.requestBuilder.setImage(img).build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse responses = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = responses.getResponses(0);

            String merchantName = null;
            BigDecimal amount = null;
            int i = 0;

            for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                //out.printf("Position : %s\n", annotation.getBoundingPoly());
                String text = annotation.getDescription();
                //out.printf("Text: %s\n", annotation.getDescription());
                //receiptContent.add(text);
                if (i == 0 && StringUtils.isNotEmpty(text)) {
                    merchantName = text;
                } else if (text.matches("-?\\d+(\\.\\d+)?")){
                    amount = new BigDecimal(text);
                }
                i++;
            }

            merchantName = merchantName.substring(0, merchantName.indexOf("\n"));
            return new ReceiptSuggestionResponse(merchantName, amount);
        }
    }
}
