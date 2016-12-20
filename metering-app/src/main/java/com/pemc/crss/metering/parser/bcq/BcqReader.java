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

import static com.pemc.crss.metering.constants.ConfigKeys.BCQ_DECLARATION_DEADLINE;
import static com.pemc.crss.metering.constants.ConfigKeys.BCQ_INTERVAL;
import static java.lang.Integer.parseInt;
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
        BcqValidator validator = new BcqValidator(getIntervalConfig(), getDeclarationConfig());
        List<List<String>> csv = getCsv(inputStream);
        return validator.getAndValidateBcq(csv);
    }

    private List<List<String>> getCsv(InputStream inputStream) throws IOException {
        List<List<String>> csv = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;
            while ((line = reader.read()) != null) {
                csv.add(line);
            }
        }
        return csv;
    }

    private int getIntervalConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper wrapper = configCache.get(BCQ_INTERVAL.toString());
        return wrapper == null ? 15 : parseInt(configCache.get(BCQ_INTERVAL.toString()).get().toString());
    }

    private int getDeclarationConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper wrapper = configCache.get(BCQ_DECLARATION_DEADLINE.toString());
        return wrapper == null ? 1 : parseInt(configCache.get(BCQ_DECLARATION_DEADLINE.toString()).get().toString());
    }

}
