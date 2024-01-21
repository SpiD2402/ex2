package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.config.RedisService;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.feignclient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.service.EnterprisesService;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import com.codigo.msexamenexp.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnterprisesServiceImpl implements EnterprisesService {

    private final EnterprisesRepository enterprisesRepository;
    private final EnterprisesValidations enterprisesValidations;
    private final DocumentsTypeRepository typeRepository;

    private final RedisService redisService;
    private  final SunatClient sunatClient;

    private final Util util;

    public EnterprisesServiceImpl(EnterprisesRepository enterprisesRepository, EnterprisesValidations enterprisesValidations, DocumentsTypeRepository typeRepository, RedisService redisService, SunatClient sunatClient, Util util) {
        this.enterprisesRepository = enterprisesRepository;
        this.enterprisesValidations = enterprisesValidations;
        this.typeRepository = typeRepository;
        this.redisService = redisService;
        this.sunatClient = sunatClient;
        this.util = util;
    }

    @Value("${token.api.sunat}")
    private String tokenSunat;

    @Value("${time.expiration.sunat.info}")
    private String timeExpirationSunactInfo;

    @Override
    public ResponseBase createEnterprise(RequestEnterprises requestEnterprises) {
        boolean validate = enterprisesValidations.validateInput(requestEnterprises);
        if(validate){
            EnterprisesEntity enterprises = getEntity(requestEnterprises);
            enterprisesRepository.save(enterprises);
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprises));
        }else{
            return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,null);
        }
    }

    @Override
    public ResponseBase findOneEnterprise(String doc) {

        EnterprisesEntity enterprisesEntity = enterprisesRepository.findByNumDocument(doc);
        if (enterprisesEntity!=null)
        {
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
        }
        else {
            return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS, Optional.empty());

        }

    }

        @Override
        public ResponseBase findAllEnterprises() {
            List<EnterprisesEntity> allEnterprisesList = enterprisesRepository.findAll()
                    .stream()
                    .filter(enterprisesEntity -> enterprisesEntity.getStatus() == 1)
                    .collect(Collectors.toList());

            Optional<List<EnterprisesEntity>> allEnterprisesOptional = Optional.of(allEnterprisesList);

            if(allEnterprisesOptional.isPresent()){
                return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,allEnterprisesOptional);
            }
            return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS,Optional.empty());
        }

    @Override
    public ResponseBase updateEnterprise(Integer id, RequestEnterprises requestEnterprises) {
            Optional<EnterprisesEntity> enterprises = enterprisesRepository.findById(id);
            boolean validationEntity = enterprisesValidations.validateInputUpdate(requestEnterprises);
            if(validationEntity){
                EnterprisesEntity enterprisesUpdate = getEnterprise(requestEnterprises,enterprises.get(),true);
                enterprisesRepository.save(enterprisesUpdate);
                return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(enterprisesUpdate));
            }else {
                return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,Optional.empty());
            }

    }

    @Override
    public ResponseBase delete(Integer id) {
        Optional<EnterprisesEntity> enterprises = enterprisesRepository.findById(id);
        if (enterprises.isPresent())
        {
            enterprises.get().setStatus(0);
            enterprises.get().setUserDelete(Constants.AUDIT_ADMIN);
            enterprises.get().setDateDelete(getTimestamp());
            enterprisesRepository.save(enterprises.get());
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of("Se elimino Correctamente"));
        }
        else
        {
            return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_NON_DATA,Optional.empty());

        }

    }


    private EnterprisesEntity getEntity(RequestEnterprises requestEnterprises){

        EnterprisesEntity entity = new EnterprisesEntity();
        ResponseSunat responseSunat =getExecutionSunat(requestEnterprises.getNumDocument());
        if(responseSunat!=null)
        {
            entity.setBusinessName(responseSunat.getRazonSocial());
            entity.setTradeName(responseSunat.getRazonSocial());
        }
        else {
            return null;
        }

        return getEnterprise(requestEnterprises,entity,false);
    }

    public EnterprisesEntity getEnterprise(RequestEnterprises requestEnterprises,EnterprisesEntity entity,boolean isUpdate) {
        entity.setNumDocument(requestEnterprises.getNumDocument());
        entity.setStatus(Constants.STATUS_ACTIVE);
        entity.setEnterprisesTypeEntity(getEnterprisesType(requestEnterprises));
        entity.setDocumentsTypeEntity(getDocumentsType());

        if (isUpdate) {
            entity.setUserModif(Constants.AUDIT_ADMIN);
            entity.setDateModif(getTimestamp());

        } else {
            entity.setUserCreate(Constants.AUDIT_ADMIN);
            entity.setDateCreate(getTimestamp());
        }
        return entity;
    }

    public EnterprisesTypeEntity getEnterprisesType(RequestEnterprises requestEnterprises){
        EnterprisesTypeEntity typeEntity = new EnterprisesTypeEntity();
        typeEntity.setIdEnterprisesType(requestEnterprises.getEnterprisesTypeEntity());
        return typeEntity;
    }

   public DocumentsTypeEntity getDocumentsType(){
        return  typeRepository.findByCodType(Constants.COD_TYPE_RUC);
    }

    public Timestamp getTimestamp(){
        return new Timestamp(System.currentTimeMillis());
    }
    public ResponseSunat getExecutionSunat(String numero){
        String redisCache= redisService.getValueByKey(Constants.REDIS_KEY_INFO_SUNAT+numero);
        if (redisCache!=null)
        {


           return Util.convertFromJson(redisCache,ResponseSunat.class);
        }
        else {
            String authorization = "Bearer "+tokenSunat;
            ResponseSunat sunat  = sunatClient.getInfoSunat(numero,authorization);
            String redisData = Util.convertToJsonEntity(sunat);
            redisService.saveKeyValue(Constants.REDIS_KEY_INFO_SUNAT+numero,redisData ,Integer.valueOf(timeExpirationSunactInfo));
            return sunat;
        }

    }
}
