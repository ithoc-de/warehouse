package de.ithoc.warehouse.config;

import de.ithoc.warehouse.persistence.entities.SyncEntity;
import de.ithoc.warehouse.persistence.entities.SyncHistory;
import de.ithoc.warehouse.persistence.repositories.SyncEntityRepository;
import de.ithoc.warehouse.persistence.repositories.SyncHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Configuration
@Slf4j
public class DataInitConfig {

    private final List<String> syncEntities;
    private final SyncEntityRepository syncEntityRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public DataInitConfig(
            @Value("${data.init.sync.entities}") List<String> syncEntities,
            SyncEntityRepository syncEntityRepository, SyncHistoryRepository syncHistoryRepository) {
        this.syncEntities = syncEntities;
        this.syncEntityRepository = syncEntityRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }


    @PostConstruct
    public void initSyncEntities() {

        for (int i = 0; i < syncEntities.size(); i += 2) {

            // Create initial sync entities if database table is still empty.
            String name = syncEntities.get(i);
            Optional<SyncEntity> syncEntityOptional = syncEntityRepository.findByName(name);
            if(syncEntityOptional.isEmpty()) {
                SyncEntity syncEntity = new SyncEntity();
                syncEntity.setName(name);
                String timestampField = syncEntities.get(i + 1);
                syncEntity.setTimestampField(timestampField);
                syncEntityOptional = Optional.of(syncEntityRepository.save(syncEntity));
            }
            SyncEntity syncEntity = syncEntityOptional.get();
            log.debug("syncEntity: {}", syncEntity);

            // Create initial sync history for each sync entity if table is still empty.
            Optional<SyncHistory> syncHistoryOptional =
                    syncHistoryRepository.findTopBySyncEntityOrderByTimestampDesc(syncEntity);
            if(syncHistoryOptional.isEmpty()) {
                SyncHistory newSyncHistory = new SyncHistory();
                newSyncHistory.setTimestamp(LocalDateTime
                        .of(2020, Month.JANUARY, 1, 0, 0, 0, 0));
                newSyncHistory.setSyncEntity(syncEntity);
                syncHistoryOptional = Optional.of(syncHistoryRepository.save(newSyncHistory));
            }
            SyncHistory syncHistory = syncHistoryOptional.get();
            log.debug("syncHistory: {}", syncHistory);
        }
    }

}
