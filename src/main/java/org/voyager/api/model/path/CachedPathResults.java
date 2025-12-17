package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Data;
import org.voyager.api.model.response.SearchStatus;
import org.voyager.commons.model.path.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Builder
@Data
public class CachedPathResults {
    List<Path> directs;
    @Builder.Default
    private List<Path> connections = new CopyOnWriteArrayList<>();
    SearchStatus status;
}
