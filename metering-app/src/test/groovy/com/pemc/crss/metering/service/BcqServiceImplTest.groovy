package com.pemc.crss.metering.service

import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.dao.BcqDao
import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.DateTimeUtils.now
import static com.pemc.crss.metering.utils.DateTimeUtils.parseDate

class BcqServiceImplTest extends Specification {

    def bcqDao = Mock(BcqDao);
    def bcqNotificationManager = Mock(BcqNotificationManager);
    def configService = Mock(CacheConfigService);
    def sut = new BcqServiceImpl(bcqDao, bcqNotificationManager, configService)

    @Unroll
    def 'save declaration by seller with status: #status'() {
        given:
        def uploadFile = new BcqUploadFile(
                fileName: 'file1',
                validationStatus: status
        )
        def header = new BcqHeader(
                sellingMtn: 'MTN1',
                billingId: 'BILL1',
                tradingDate: now()
        )
        def headerList = [header]
        def declaration = new BcqDeclaration(
                uploadFileDetails: new BcqUploadFileDetails(uploadFile),
                sellerDetails: new ParticipantSellerDetails(name: 'Gen', shortName: 'GEN1'),
                headerDetailsList: [new BcqHeaderDetails(header)],
                validationResult: new BcqValidationResult(status: status),
                isResubmission: isResubmission
        )

        when:
        sut.saveDeclaration(declaration, false)

        then:
        if (status == ACCEPTED) {
            1 * bcqDao.saveUploadFile(_ as BcqUploadFile)
            1 * bcqDao.saveHeaders(_ as List, _ as Boolean) >> []
            if (isResubmission) {
                1 * bcqDao.findAllHeaders(_ as Map) >> headerList
            }
            1 * bcqNotificationManager.sendUploadNotification(_ as List)
        } else {
            1 * bcqNotificationManager.sendValidationNotification(_ as BcqDeclaration, _ as Date)
        }

        where:
        isResubmission << [true, false, true, false]
        status << [ACCEPTED, ACCEPTED, REJECTED, REJECTED]
    }

    @Unroll
    def 'save declaration by settlement with status: #status'() {
        given:
        def uploadFile = new BcqUploadFile(
                fileName: 'file1',
                validationStatus: status
        )
        def header = new BcqHeader(
                sellingMtn: 'MTN1',
                billingId: 'BILL1',
                tradingDate: parseDate('12/12/2012')
        )
        def declaration = new BcqDeclaration(
                uploadFileDetails: new BcqUploadFileDetails(uploadFile),
                sellerDetails: new ParticipantSellerDetails(name: 'Gen', shortName: 'GEN1'),
                headerDetailsList: [new BcqHeaderDetails(header)],
                validationResult: new BcqValidationResult(status: status)
        )

        when:
        sut.saveDeclaration(declaration, true)

        then:
        if (status == ACCEPTED) {
            1 * bcqDao.saveUploadFile(_ as BcqUploadFile)
            1 * bcqDao.saveHeaders(_ as List, _ as Boolean) >> []
            1 * bcqDao.findAllHeaders(_ as Map) >> headerList
            1 * bcqNotificationManager.sendSettlementUploadNotification(_ as List)
        } else {
            1 * bcqNotificationManager.sendSettlementValidationNotification(_ as BcqDeclaration, _ as Date)
        }

        where:
        status << [ACCEPTED, ACCEPTED, REJECTED, REJECTED]
        headerList << [
                [new BcqHeader(
                        sellingMtn: 'MTN1',
                        billingId: 'BILL1',
                        tradingDate: parseDate('12/12/2012')
                ), new BcqHeader(
                        sellingMtn: 'MTN2',
                        billingId: 'BILL2',
                        tradingDate: parseDate('12/12/2012')
                )
                ],
                [],
                [],
                []
        ]
    }

}
