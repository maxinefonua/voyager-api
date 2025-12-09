package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Data;
import org.voyager.api.model.response.SearchStatus;
import java.util.List;

@Builder @Data
public class CachedSearchResponse {
    private List<PathDetailed> content;
    private SearchStatus status;
    private boolean hasMore;
}
