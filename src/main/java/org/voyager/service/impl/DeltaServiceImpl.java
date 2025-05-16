package org.voyager.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.Delta;
import org.voyager.repository.DeltaRepository;
import org.voyager.service.DeltaService;
import org.voyager.service.utils.MapperUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeltaServiceImpl implements DeltaService {
    @Autowired
    DeltaRepository deltaRepository;

    @Override
    public DeltaDisplay save(DeltaForm deltaForm) {
        return MapperUtils.deltaToDisplay(deltaRepository.save(MapperUtils.formToDelta(deltaForm)));
    }

    @Override
    public DeltaDisplay patch(DeltaDisplay deltaDisplay, DeltaPatch deltaPatch) {
        return MapperUtils.deltaToDisplay(deltaRepository.save(MapperUtils.patchDisplayToDelta(deltaDisplay,deltaPatch)));
    }

    @Override
    public Option<DeltaDisplay> getByIata(String iata) {
        Optional<Delta> deltaOptional = deltaRepository.findById(iata);
        if (deltaOptional.isEmpty()) return Option.none();
        return Option.of(MapperUtils.deltaToDisplay(deltaOptional.get()));
    }

    @Override
    public List<DeltaDisplay> getAll() {
        return deltaRepository.findAll().stream().map(MapperUtils::deltaToDisplay).toList();
    }

    @Override
    public List<DeltaDisplay> getAllByStatusList(List<DeltaStatus> statusList) {
        return deltaRepository.findByStatusIn(statusList).stream().map(MapperUtils::deltaToDisplay).toList();
    }

    @Override
    public List<String> getActiveCodes() {
        return deltaRepository.selectActiveSeasonalIataOrderByIata();
    }

    @Override
    public Boolean exists(String iata) {
        return deltaRepository.existsById(iata);
    }
}
