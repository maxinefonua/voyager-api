package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaServiceImpl.class);

    @Override
    public Delta save(DeltaForm deltaForm) {
        DeltaEntity deltaEntity = MapperUtils.formToDeltaEntity(deltaForm);
        try {
            deltaEntity = deltaRepository.save(deltaEntity);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositorySaveErrorMessageWithIata("delta entity",deltaForm.getIata()),
                    e);
        }
        return MapperUtils.entityToDelta(deltaEntity);
    }

    @Override
    public Delta patch(Delta delta, DeltaPatch deltaPatch) {
        DeltaEntity deltaEntity = MapperUtils.patchToDeltaEntity(delta,deltaPatch);
        try {
            deltaEntity = deltaRepository.save(deltaEntity);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositoryPatchErrorMessage("delta entity",delta.getIata()),
                    e);
        }
        return MapperUtils.entityToDelta(deltaEntity);
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
