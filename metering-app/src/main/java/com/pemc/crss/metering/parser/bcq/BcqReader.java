package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.dto.BcqDetails;
import com.pemc.crss.metering.validator.BcqValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.pemc.crss.metering.constants.ConfigKeys.BCQ_INTERVAL;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
@Component
public class BcqReader {

    private final CacheManager cacheManager;

    @Autowired
    public BcqReader(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public BcqDetails readData(InputStream inputStream) throws IOException {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper intervalWrapper = configCache.get(BCQ_INTERVAL.toString());
        int intervalConfig = intervalWrapper == null ? 15 :
                Integer.parseInt(configCache.get(BCQ_INTERVAL.toString()).get().toString());
        BcqValidator validator = new BcqValidator(intervalConfig);
        List<List<String>> csv = new ArrayList<>();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;

            while ((line = reader.read()) != null) {
                csv.add(line);
            }
        }

        return validator.getAndValidateBcq(csv);
    }

}
