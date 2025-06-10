package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.delta.Delta;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;

import java.util.List;

// TODO: implement method to invalidate delta caches
public interface DeltaService {
    List<String> getActiveCodes();
    Boolean exists(String iata);
}
