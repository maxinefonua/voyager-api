package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Data;
import org.voyager.api.model.response.SearchStatus;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Builder @Data
public class CachedSearchResponse {
    @Builder.Default
    private List<PathDetailed> content = new CopyOnWriteArrayList<>();
    private SearchStatus status;
    boolean hasMore;
    private int converting;
    private int converted;
}
