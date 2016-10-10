package com.pemc.crss.metering.resource;

import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dao.MeteringDao;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterDataXLS;
import com.pemc.crss.metering.dto.MeterUploadHeader;
import com.pemc.crss.metering.dto.MeterUploadMDEF;
import com.pemc.crss.metering.parser.ExcelReader;
import com.pemc.crss.metering.parser.MDEFReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class MeteringResource {

    @Autowired
    private MeteringDao meteringDao;

    @RequestMapping("/xls")
    @Transactional
    public long xls() throws IOException {
        ExcelReader reader = new ExcelReader();

        String[] files = new String[]{
                "MET_CEDCMSP_R3MEXCEDC01TNSC01_20161003.xls",
                "MF3MABAMSUZ01.xls",
                "MF3MMEXRASL02.xls",
                "R3MEXCEDC01TNSC01.xls",
                "R3MEXSFEL01SMC401.xls",
                "R3MEXSFEL02PCBC01.xls",
                "R3MEXSFEL02SMCB01.xls"
        };

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (String file : files) {
            System.out.println("File:" + file);
            List<MeterDataXLS> meterDataList = reader.readExcel(
                    new FileInputStream(
                            new File(MeteringResource.class.getClassLoader().getResource(file).getFile())));

            meteringDao.saveMeterUploadXLS(1, meterDataList);
        }

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        return 0;
    }

    @RequestMapping("/hello")
    @Transactional
    public long hello() throws Exception {
        MeterUploadHeader uploadHeader = new MeterUploadHeader();
        long i = meteringDao.saveMeterUploadHeader(uploadHeader);

        MeterUploadMDEF file = new MeterUploadMDEF();
        file.setFileName("test_file.mdef");
        file.setFileType("MDEF");
        file.setFileSize(123L);
        file.setStatus("Accepted");
        long fileID = meteringDao.saveMeterUploadFile(i, file);

        String[] files = new String[]{
                "DT005958.MDE",
                "DT023122.MDE",
                "DT023693.MDE",
                "DT024256.MDE",
                "DT030188.MDE",
                "DT031131.MDE"
        };

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (String mdefFile : files) {
            System.out.println("Processing:" + mdefFile);
            MDEFReader reader = new MDEFReader();
            MeterData meterData = reader.readMDEF(
                    new FileInputStream(
                            new File(MeteringResource.class.getClassLoader().getResource(mdefFile).getFile())));

            meteringDao.saveMeterUploadMDEF(fileID, meterData);
        }
        stopWatch.stop();
        System.out.println("Total time:" + stopWatch.getTotalTimeSeconds());

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

    @PostMapping("/upload")
    public ResponseEntity<String> testUpload(MultipartHttpServletRequest request, @RequestParam("uploadType") UploadType uploadType) {
        Map<String, MultipartFile> fileMap = request.getFileMap();

        for(MultipartFile file : fileMap.values()) {
            System.out.println(file.getOriginalFilename());
        }
        System.out.println(uploadType);
        return new ResponseEntity<>("Successfully parsed", HttpStatus.OK);
    }

}
