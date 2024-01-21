package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.config.RedisService;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.feignclient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import com.codigo.msexamenexp.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EnterprisesServiceImplTest {

    @Mock

    EnterprisesRepository enterprisesRepository;
    @Mock
     EnterprisesValidations enterprisesValidations;
    @Mock
    DocumentsTypeRepository typeRepository;

    @Mock
   RedisService redisService;
    @Mock
    SunatClient sunatClient;

    @Mock
   Util util;

    @InjectMocks
    EnterprisesServiceImpl enterprisesService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        enterprisesService = new EnterprisesServiceImpl(enterprisesRepository,enterprisesValidations,typeRepository,redisService,sunatClient,util);
    }

    @Test
    void findAllEnterprises() {
        EnterprisesEntity enterprises = new EnterprisesEntity(1,"20552103816",
                "Empresa Sac","Empresa",1,null,null);

        EnterprisesEntity enterprises2= new EnterprisesEntity(1,"20552103816",
                "Empresa Sac","Empresa",1,null,null);

        List<EnterprisesEntity> enterprisesEntities  = List.of(enterprises,enterprises2);

        Mockito.when(enterprisesRepository.findAll((Example<EnterprisesEntity>) Mockito.any())).thenReturn(enterprisesEntities);

        ResponseBase responseBase2 = enterprisesService.findAllEnterprises();

        ResponseBase responseBaseBueno =  new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntities));
        assertNotNull(responseBaseBueno);
        assertNotNull(responseBase2.getData());




    }

    @Test
        void findAllError()
        {

            List<EnterprisesEntity> enterprisesEntities = Collections.emptyList();

            Mockito.when(enterprisesRepository.findAll((Example<EnterprisesEntity>) Mockito.any())).thenReturn(enterprisesEntities);

            ResponseBase responseBaseMalo =  new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS,Optional.of(List.of()));

            ResponseBase responseBase2 =  enterprisesService.findAllEnterprises();

            assertEquals(responseBaseMalo.getData(),responseBase2.getData());


        }

    @Test
    void findOneEnterprise() {

        String document ="20552103816";
        EnterprisesEntity enterprises=new EnterprisesEntity(1,"20552103816","Empresa Sac","Empresa",1,null,null);

        ResponseBase responseBase = new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprises));
        Mockito.when(enterprisesRepository.findByNumDocument(Mockito.any())).thenReturn(enterprises);
        ResponseBase responseBase2 =  enterprisesService.findOneEnterprise(document);
        assertEquals(responseBase.getCode(),responseBase2.getCode());
        assertEquals(responseBase.getMessage(),responseBase2.getMessage());
        assertEquals(responseBase.getData(),responseBase2.getData());
        Mockito.verify(enterprisesRepository).findByNumDocument(document);


    }

    @Test
    void findOneError()
    {
        String document ="20552103816";
        EnterprisesEntity  enterprises =null;

        ResponseBase responseNegativo= new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS, Optional.empty());

        Mockito.when(enterprisesRepository.findByNumDocument(Mockito.anyString())).thenReturn(enterprises);

        ResponseBase responseBase= enterprisesService.findOneEnterprise(document);

        assertEquals(responseBase.getCode(),responseNegativo.getCode());



    }

    @Test
    void getEnterpriseTypeTest ()
    {
        RequestEnterprises requestEnterprises = new RequestEnterprises("7854544","tienda","asda",1,1);
        EnterprisesTypeEntity entity = new EnterprisesTypeEntity();
        entity.setIdEnterprisesType(requestEnterprises.getEnterprisesTypeEntity());

        EnterprisesTypeEntity entity2 = enterprisesService.getEnterprisesType(requestEnterprises);

        assertEquals(entity.getIdEnterprisesType(),entity2.getIdEnterprisesType());


    }

    @Test
    void getTimestampTest()
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Timestamp timestamp1 = enterprisesService.getTimestamp();

        assertEquals(timestamp1.getTime(),timestamp.getTime());



    }

    @Test
    void getEnterpriseTest()
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        EnterprisesTypeEntity type = new EnterprisesTypeEntity();
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity();
        boolean isRes = true;
        EnterprisesEntity enterprises = new EnterprisesEntity();
        RequestEnterprises requestEnterprises = new RequestEnterprises("7854544","tienda","asda",1,1);
        enterprises.setNumDocument(requestEnterprises.getNumDocument());
        enterprises.setStatus(Constants.STATUS_ACTIVE);
        enterprises.setEnterprisesTypeEntity(type);
        enterprises.setDocumentsTypeEntity(documentsTypeEntity);
        enterprises.setUserModif(Constants.AUDIT_ADMIN);
        enterprises.setDateModif(timestamp);
        EnterprisesEntity bueno = enterprisesService.getEnterprise(requestEnterprises,enterprises,isRes);


        assertEquals(bueno.getIdEnterprises(),enterprises.getIdEnterprises());
        assertEquals(bueno.getNumDocument(),enterprises.getNumDocument());

    }

    @Test
    void getEnterpriseTestError()
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        EnterprisesTypeEntity type = new EnterprisesTypeEntity();
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity();
        boolean isRes = false;
        EnterprisesEntity enterprises = new EnterprisesEntity();
        RequestEnterprises requestEnterprises = new RequestEnterprises("7854544","tienda","asda",1,1);
        enterprises.setNumDocument(requestEnterprises.getNumDocument());
        enterprises.setStatus(Constants.STATUS_ACTIVE);
        enterprises.setEnterprisesTypeEntity(type);
        enterprises.setDocumentsTypeEntity(documentsTypeEntity);
        enterprises.setUserCreate(Constants.AUDIT_ADMIN);
        enterprises.setDateCreate(timestamp);

        EnterprisesEntity bueno = enterprisesService.getEnterprise(requestEnterprises,enterprises,isRes);

        assertEquals(bueno.getIdEnterprises(),enterprises.getIdEnterprises());
        assertEquals(bueno.getNumDocument(),enterprises.getNumDocument());

    }


    @Test
    void deleteTest()
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Integer id = 1;

        Optional<EnterprisesEntity>enterprises = Optional.of(new EnterprisesEntity());
        Mockito.when(enterprisesRepository.findById(id)).thenReturn(enterprises);
        enterprises.get().setStatus(0);
        enterprises.get().setStatus(0);
        enterprises.get().setUserDelete(Constants.AUDIT_ADMIN);
        enterprises.get().setDateDelete(timestamp);
        Mockito.when(enterprisesRepository.save(enterprises.get())).thenReturn(enterprises.get());

        ResponseBase esperado = enterprisesService.delete(id);

        assertNotNull(esperado.getData());

    }



    @Test
    void getEntityTest()
    {


        RequestEnterprises requestEnterprises=new RequestEnterprises();

        EnterprisesEntity entity = new EnterprisesEntity();
        entity.setBusinessName("responseSunat.getRazonSocial()");
        entity.setTradeName("responseSunat.getRazonSocial()");

        EnterprisesEntity enterprises= enterprisesService.getEntity(requestEnterprises);

    }



}