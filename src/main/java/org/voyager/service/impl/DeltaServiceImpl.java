package org.voyager.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.model.delta.Delta;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.DeltaEntity;
import org.voyager.repository.DeltaRepository;
import org.voyager.service.DeltaService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;
import java.util.Optional;

@Service
public class DeltaServiceImpl implements DeltaService {
    @Autowired
    DeltaRepository deltaRepository;

    @Override
    public Delta save(DeltaForm deltaForm) {
        return MapperUtils.entityToDelta(deltaRepository.save(MapperUtils.formToDeltaEntity(deltaForm)));
    }

    @Override
    public Delta patch(Delta delta, DeltaPatch deltaPatch) {
        return MapperUtils.entityToDelta(deltaRepository.save(MapperUtils.patchToDeltaEntity(delta,deltaPatch)));
    }

    @Override
    public Option<Delta> getByIata(String iata) {
        Optional<DeltaEntity> deltaOptional = deltaRepository.findById(iata);
        if (deltaOptional.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToDelta(deltaOptional.get()));
    }

    @Override
    public List<Delta> getAll() {
        return deltaRepository.findAll().stream().map(MapperUtils::entityToDelta).toList();
    }

    @Override
    public List<Delta> getAllByStatusList(List<DeltaStatus> statusList) {
        return deltaRepository.findByStatusIn(statusList).stream().map(MapperUtils::entityToDelta).toList();
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
