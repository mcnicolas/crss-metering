package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.dto.MeterUploadMDEF;
import com.pemc.crss.metering.parser.MDEFReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;

@RestController
public class MeteringResource {

    @Autowired
    private MeteringDao meteringDao;

    @RequestMapping("/hello")
    @Transactional
    public long hello() throws Exception {
        MeterUploadHeader uploadHeader = new MeterUploadHeader();
        long i = meteringDao.saveMeterUploadHeader(uploadHeader);

        MeterUploadMDEF file = new MeterUploadMDEF();
        file.setFileName("test_file.mdef");
        file.setFileType("MDEF");
        file.setFileSize(123L);
        file.setChecksum("asdf");
        file.setStatus("Accepted");
        long fileID = meteringDao.saveMeterUploadFile(i, file);

        MDEFReader reader = new MDEFReader();
        MeterData meterData = reader.readMDEF(
                new FileInputStream(
                        new File(MeteringResource.class.getClassLoader().getResource("DT005958.MDE").getFile())));

        meteringDao.saveMeterUploadMDEF(fileID, meterData);

/*

        MeterUploadMDEF file = new MeterUploadMDEF();
        file.setFileName("test_file.mdef");
        file.setFileType("MDEF");
        file.setFileSize(123L);
        file.setChecksum("asdf");
        file.setStatus("Accepted");
        long fileID = meteringDao.saveMeterUploadFile(i, file);

        MeterData meterData = new MeterData();
        Header header = new Header();
        header.setCustomerID("1");
        header.setCustomerName("asdf");
        header.setCustomerAddress1("asdf");
        header.setCustomerAddress2("asdf");
        header.setCustomerAccountNo("asdf");
        header.setTotalChannels("1234");
        header.setStartTime("asdf");
        header.setStopTime("asdf");
        header.setDstFlag("Y");

        ChannelHeader channelHeader = new ChannelHeader();
        channelHeader.setCustomerID("1234");
        channelHeader.setRecorderID("12");
        channelHeader.setMeterNo("12");
        channelHeader.setStartTime("123");
        channelHeader.setStopTime("123");
        channelHeader.setMeterChannelNo("1");
        channelHeader.setCustomerChannelNo(1);
        channelHeader.setUomCode("123");
        channelHeader.setChannelStatusPresent("Y");
        channelHeader.setIntervalStatusPresent("Y");
        channelHeader.setStartMeterReading("123");
        channelHeader.setStopMeterReading("123");
        channelHeader.setMeterMultiplier("123");
        channelHeader.setServerType("A");
        channelHeader.setIntervalPerHour(123);
        channelHeader.setValidationResults("12");
        channelHeader.setPowerFlowDirection("1");
        channelHeader.setKvaSet(1);
        channelHeader.setDataOrigin("1");
        meterData.addChannel(channelHeader);

        meterData.setHeader(header);

        meteringDao.saveMeterUploadMDEF(fileID, meterData);

        MeterUploadXLS xls = new MeterUploadXLS();
        xls.setFileID(1);
        xls.setCustomerID("Customer 1");
        xls.setReadingDateTime("xxx");
        xls.setMeterNo("meter no");
        xls.setChannelStatus("chan stat");
        xls.setChannelStatusDesc("chan stat desc");
        xls.setIntervalStatus("int stat");
        xls.setIntervalStatusDesc("int stat desc");
        meteringDao.saveMeterUploadXLS(i, xls);
*/

        return i;
    }

}
