package org.voyager.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.api.model.path.PathDetailed;
import java.util.List;

@Builder @NoArgsConstructor
@AllArgsConstructor @Data
public class SearchResponse {
    private List<PathDetailed> content;
    private SearchStatus status;
    private boolean hasMore;
    private Integer totalFound;
    private int size;
}
