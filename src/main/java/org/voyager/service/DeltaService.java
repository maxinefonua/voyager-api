package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;

import java.util.List;

public interface DeltaService {
    DeltaDisplay save(DeltaForm deltaForm);
    DeltaDisplay patch(DeltaDisplay deltaDisplay, DeltaPatch deltaPatch);
    Option<DeltaDisplay> getByIata(String iata);
    List<DeltaDisplay> getAll();
    List<DeltaDisplay> getAllByStatusList(List<DeltaStatus> statusList);
    List<String> getActiveCodes();
    Boolean exists(String iata);
}
