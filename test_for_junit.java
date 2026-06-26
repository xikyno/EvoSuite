package org.openbdfc.devops.integration.service.adpm.impl;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openbdfc.deploy.opt.auth.client.api.UserCenterApi;
import org.openbdfc.deploy.opt.auth.client.entity.UserInfoDTO;
import org.openbdfc.deploy.opt.auth.client.entity.UserNameListReqDTO;
import org.openbdfc.devops.integration.common.constant.ExecutorConstant;
import org.openbdfc.devops.integration.common.domain.BaseServiceImpl;
import org.openbdfc.devops.integration.common.domain.PageResult;
import org.openbdfc.devops.integration.common.enums.DevopsCodeEnums;
import org.openbdfc.devops.integration.common.enums.LastCICDStatusEnums;
import org.openbdfc.devops.integration.common.enums.NormalDetStusEnums;
import org.openbdfc.devops.integration.common.exception.BussinessException;
import org.openbdfc.devops.integration.common.exception.DevopsRuntimeException;
import org.openbdfc.devops.integration.common.utils.IGenerator;
import org.openbdfc.devops.integration.common.utils.JacksonUtil;
import org.openbdfc.devops.integration.entity.*;
import org.openbdfc.devops.integration.repository.dto.adpm.DeliverVersionDto;
import org.openbdfc.devops.integration.repository.mapper.adpm.InvestBatchLinkSysManageApplicationMapper;
import org.openbdfc.devops.integration.repository.mapper.adpm.InvestBatchMapper;
import org.openbdfc.devops.integration.repository.mapper.batch.InvestBatchLinkPasoMapper;
import org.openbdfc.devops.integration.repository.mapper.continues.DeployGroupItemMapper;
import org.openbdfc.devops.integration.repository.mapper.continues.DeployGroupMapper;
import org.openbdfc.devops.integration.repository.mapper.paso.ArchAppMapper;
import org.openbdfc.devops.integration.repository.mapper.releasedelivery.*;
import org.openbdfc.devops.integration.repository.mapper.requirement.RequireMapper;
import org.openbdfc.devops.integration.repository.mapper.system.UserMapper;
import org.openbdfc.devops.integration.service.adpm.InvestBatchDeliverVersionService;
import org.openbdfc.devops.integration.service.releasedelivery.impl.RequirementOrchestrationServiceImpl;
import org.openbdfc.devops.integration.service.submittest.PublishingWindowService;
import org.openbdfc.devops.integration.vo.adpm.*;
import org.openbdfc.devops.integration.vo.submittest.DeliveryTypeAndBuildTypeVO;
import org.openbdfc.devops.integration.vo.submittest.DeliveryVersionArtifactInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.openbdfc.devops.integration.common.constant.ExecutorConstant.*;

/**
 * @description: 批次详情
 * @author 沈文杰
 * @date 2025-10-13 18:03
 */
@Slf4j
@Service
public class InvestBatchDeliverVersionServiceImpl extends BaseServiceImpl<PublishingWindowMapper, PublishingWindowEntity> implements InvestBatchDeliverVersionService {

    @Autowired
    private InvestBatchMapper investBatchMapper;

    @Autowired
    private ArchAppMapper archAppMapper;

    @Autowired
    private InvestBatchLinkPasoMapper investBatchLinkPasoMapper;

    @Autowired
    private SysRequirementMapper sysRequirementMapper;

    @Autowired
    private RelDevelopmentTaskSysRequirementMapper relDevelopmentTaskSysRequirementMapper;

    @Autowired
    private RelDeliveryVersionSysRequirementMapper relDeliveryVersionSysRequirementMapper;

    @Autowired
    private DeployGroupMapper deployGroupMapper;

    @Autowired
    private DeployGroupItemMapper deployGroupItemMapper;

    @Autowired
    private RelComponentsDevelopmentTaskMapper relComponentsDevelopmentTaskMapper;

    @Autowired
    private PublishingWindowService publishingWindowService;

    @Autowired
    private DevelopmentTaskMapper developmentTaskMapper;

    @Autowired
    private PublishingWindowMapper publishingWindowMapper;

    @Autowired
    private RequireMapper requireMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvestBatchLinkSysManageApplicationMapper investBatchLinkSysManageApplicationMapper;

    @Autowired
    private UserCenterApi userCenterApi;


    /**
     * 批次详情-交付版本列表
     */
    @Override
    public List<BatchDeliverResVo> deliverVersionListQuery(BatchItemDetailQueryVo batchItemDetailQueryVo) {
        log.info("批次详情-交付版本列表-入参：{}", JacksonUtil.obj2String(batchItemDetailQueryVo));
        InvestBatchEntity batchEntity = investBatchMapper.selectById(batchItemDetailQueryVo.getBatchId());
        List<DeliverVersionDto> deliverList = publishingWindowMapper.
                selectBatchOfDeliverList(batchEntity.getAdpmBatchId(),batchItemDetailQueryVo.getKeywords());
        List<BatchDeliverResVo> resVos = new ArrayList<>();

        //该应用暂无交付版本
        List<Long> batchIds = new ArrayList<>();
        batchIds.add(batchItemDetailQueryVo.getBatchId());
        List<InvestBatchLinkSysManageApplicationEntity> entities = investBatchLinkSysManageApplicationMapper.listMoreByBatchIds(batchIds);
        if(CollectionUtils.isNotEmpty(entities)){
            List<String> appNames = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(deliverList)){
                appNames = deliverList.stream().map(DeliverVersionDto::getAppName).toList();
            }
            //赛选批次下的应用系统
            List<String> finalAppNames = appNames;
            List<InvestBatchLinkSysManageApplicationEntity> list = entities.stream().filter(item -> !finalAppNames.contains(item.getAdpmSysChargePerson())).toList();
            //有关键字检索
            if(CollectionUtils.isNotEmpty(list) && StringUtils.isNotEmpty(batchItemDetailQueryVo.getKeywords())){
                list=list.stream().filter(item -> item.getAdpmSysChargePerson().contains(batchItemDetailQueryVo.getKeywords()))
                        .collect(Collectors.toList());
            }
            for(InvestBatchLinkSysManageApplicationEntity e:list){
                BatchDeliverResVo resVo=new BatchDeliverResVo();
                //填充异常信息
                List<String> exceptions = new ArrayList<>();
                exceptions.add(EXCEPTION_SYS_NONE_IN_APP);
                resVo.setExceptions(exceptions);
                resVo.setAppName(e.getAdpmSysChargePerson());
                resVos.add(resVo);
            }
        }

        if(CollectionUtils.isNotEmpty(deliverList)){
            // 拿到所有用户真实名称
            List<String> userNames = deliverList.stream().map(DeliverVersionDto::getAppOwnerName).collect(Collectors.toList());
            List<UserInfoDTO> userInfoDTOS = userCenterApi.queryUsersByUserNames(new UserNameListReqDTO(userNames));
            Map<String, String> userNameAndRealNameMaps;
            if (CollectionUtils.isNotEmpty(userInfoDTOS)){
                userNameAndRealNameMaps = userInfoDTOS.stream().collect(Collectors.toMap(UserInfoDTO::getUserName, UserInfoDTO::getRealName));
            } else {
                userNameAndRealNameMaps = Collections.emptyMap();
            }

            deliverList.forEach(e->{
                BatchDeliverResVo convert = IGenerator.convert(e, BatchDeliverResVo.class);
                //应用需求数
                int bindCount = relDeliveryVersionSysRequirementMapper.selectCount(Wrappers.<RelDeliveryVersionSysRequirementEntity>lambdaQuery()
                        .eq(RelDeliveryVersionSysRequirementEntity::getDeliveryVersionId, e.getId())
                        .eq(RelDeliveryVersionSysRequirementEntity::getDeleted, NormalDetStusEnums.Normal.getCode()))
                        .intValue();
                convert.setSysReqCount(bindCount);
                //组件数量
                convert.setComponentCount(publishingWindowMapper.countComponentOfDeliver(e.getProjectMainId(), e.getId(),null));
                //用户信息
                if(StringUtils.isNotEmpty(e.getAppOwnerName())){
                    convert.setAppOwnerName(userNameAndRealNameMaps.get(e.getAppOwnerName()));
                }


                //填充异常信息
                List<String> exceptions = new ArrayList<>();
                if(0 >= bindCount){
                    exceptions.add(EXCEPTION_SYS_NONE_IN_VER);
                }
                /*if(CollectionUtils.isNotEmpty(componentEntities)){
                    DeliveryArtifactQueryVo queryVo = new DeliveryArtifactQueryVo();
                    for (ArchRelAppComponentEntity entity:componentEntities) {
                        //添加未部署最新异常提示
                        queryVo.setDeliveryId(Long.parseLong(e.getId()));
                        queryVo.setSystemId(entity.getId());
                        queryVo.setSysAbb(entity.getSysAbb());
                        List<DeliveryTypeAndBuildTypeVO> buildTypeVOS = publishingWindowService.artifactListOfComponentQuerySimple(queryVo);
                        if(CollectionUtils.isNotEmpty(buildTypeVOS)){
                            buildTypeVOS.forEach(v->{
                                if(!exceptions.contains(EXCEPTION_NEW_NONE_IN_VER)){
                                    DeliveryVersionArtifactInfoVo cd = v.getArtifactInfoList().stream().filter
                                            (item -> item.getPipelineId() != null && item.getCdPipelineId() != null &&
                                                    item.getPipelineId().equals(item.getCdPipelineId())).findFirst().orElse(null);
                                    if(Objects.isNull(cd)){
                                        exceptions.add(EXCEPTION_NEW_NONE_IN_VER);
                                    }
                                }
                                //添加未部署所有组件的异常提示
                                if(!exceptions.contains(EXCEPTION_NONE_IN_CD)){
                                    DeliveryVersionArtifactInfoVo cdNone = v.getArtifactInfoList().stream().filter
                                            (item -> item.getCdPipelineId() == null).findFirst().orElse(null);
                                    if(!Objects.isNull(cdNone)){
                                        exceptions.add(EXCEPTION_NONE_IN_CD);
                                    }
                                }
                            });
                        }
                        if(exceptions.contains(EXCEPTION_NEW_NONE_IN_VER) && exceptions.contains(EXCEPTION_NONE_IN_CD)){
                            break;
                        }
                    }
                }*/
                convert.setExceptions(exceptions);
                resVos.add(convert);
            });
        }
        log.info("批次详情-交付版本列表-出参：{}", JacksonUtil.obj2String(resVos));
        return resVos;
    }

    /**
     * 批次详情-交付版本-组件列表
     */
    @Override
    public List<ComponentOfDeliverResVo> componentListOfDeliveryQuery(ComponentDeliveryQueryVo detailQueryVo) {
        log.info("批次详情-交付版本的组件列表-入参：{}", JacksonUtil.obj2String(detailQueryVo));
        List<ComponentOfDeliverResVo> resVos = new ArrayList<>();
        PublishingWindowEntity windowEntity = publishingWindowMapper.selectById(detailQueryVo.getId());
        List<ArchRelAppComponentEntity> componentEntities = publishingWindowMapper.selectComponentOfDeliver(windowEntity.getProjectMainId(), windowEntity.getId().toString(),detailQueryVo.getKeywords());
        if(CollectionUtils.isNotEmpty(componentEntities)){
            componentEntities.forEach(e->{
                ComponentOfDeliverResVo  resVo = new ComponentOfDeliverResVo();
                resVo.setId(e.getId());
                resVo.setSysAbb(e.getSysAbb());
                resVos.add(resVo);
            });
        }
        log.info("批次详情-交付版本的组件列表-出参：{}", JacksonUtil.obj2String(resVos));
        return resVos;
    }

    /**
     * 批次详情-交付版本的应用需求列表
     */
    @Override
    public List<SysReqOfDeliverResVo> sysReqDeliverQuery(ComponentDeliveryQueryVo deliveryQueryVo) {
        log.info("批次详情-交付版本的应用需求列表-入参：{}", JacksonUtil.obj2String(deliveryQueryVo));
        //应用需求列表
        List<SysRequirementEntity> entityList = relDeliveryVersionSysRequirementMapper
                .querySysReqByDeliver(deliveryQueryVo.getId());
        List<SysReqOfDeliverResVo> resVos= new ArrayList<>();
        if(CollectionUtils.isNotEmpty(entityList)){
            entityList.forEach(e->{
                SysReqOfDeliverResVo resVo= new SysReqOfDeliverResVo();
                resVo.setSysReqId(e.getTid());
                resVo.setSourceNo(e.getSourceNo());
                resVo.setSysReqStatus(e.getReqStatus());
                resVo.setSysReqStatusDesc
                        (RequirementOrchestrationServiceImpl.DEVOPS_TO_ONES_STATUS.get(e.getReqStatus()).getName());
                resVo.setSysReqName(e.getReqTitle());
                //应用需求的任务
                List<DevelopmentTaskEntity> taskEntities = developmentTaskMapper.findTaskBySysReqId(e.getTid());
                if(CollectionUtils.isNotEmpty(taskEntities)){
                    List<TaskOfSysReqResVo> taskList=new ArrayList<>();
                    taskEntities.forEach(s->{
                        TaskOfSysReqResVo reqResVo= new TaskOfSysReqResVo();
                        reqResVo.setTaskId(s.getTid());
                        reqResVo.setSourceNo(s.getTaskNo());
                        reqResVo.setTaskName(s.getTaskTitle());
                        reqResVo.setTaskOwnerName(s.getDevOwnerName());
                        reqResVo.setTaskStatus(s.getTaskStatus());
                        reqResVo.setTaskStatusDesc
                                (RequirementOrchestrationServiceImpl.DEVOPS_TO_ONES_STATUS.get(s.getTaskStatus()).getName());
                        //任务关联的组件
                        ArchRelAppComponentEntity component = developmentTaskMapper.findComponentByTaskId(s.getTid());
                        if(!Objects.isNull(component)){
                            reqResVo.setComponentName(component.getSysName());
                        }
                        taskList.add(reqResVo);
                    });
                    resVo.setTaskList(taskList);
                }
                if(CollectionUtils.isNotEmpty(resVo.getTaskList())){
                    resVo.setComponentNames(resVo.getTaskList().stream()
                            .map(TaskOfSysReqResVo::getComponentName).toList().stream().distinct().toList());
                }
                resVos.add(resVo);
            });
        }
        log.info("批次详情-交付版本的应用需求列表-出参：{}", JacksonUtil.obj2String(resVos));
        return resVos;
    }

    /**
     * 批次详情-组件与环境
     */
    @Override
    public PageResult<BatchAppComponentParVo> componentEnvPageQuery(ComponentEnvQueryPageVo pageVo) {
        log.info("批次详情-组件与环境-入参：{}", JacksonUtil.obj2String(pageVo));
        //批次与版本
        InvestBatchEntity investBatchEntity = investBatchMapper.selectById(pageVo.getBatchId());
        List<PublishingWindowEntity> deliverEntities = publishingWindowMapper.selectDeliverList(investBatchEntity.getAdpmBatchId());
        List<BatchArtifactInfoDto> allData = new ArrayList<>();

        for(PublishingWindowEntity windowEntity:deliverEntities){
            //查询版本关联的任务和组件
            List<String> itemReqNumbers=new ArrayList<>();
            ArchAppEntity appEntity = archAppMapper.selectById(windowEntity.getProjectMainId());
            List<SysRequirementEntity> sysReqEntities = relDeliveryVersionSysRequirementMapper.querySysReqByDeliver(windowEntity.getId());
            if(CollectionUtils.isNotEmpty(sysReqEntities)){
                List<Long> sysReqIds = sysReqEntities.stream().map(SysRequirementEntity::getTid).toList();
                List<RequireItemEntity> itemEntities = relDeliveryVersionSysRequirementMapper.queryItemReqBySysReqIds(sysReqIds);
                itemReqNumbers = itemEntities.stream().map(RequireItemEntity::getRequireItemNo).toList();
            }
            List<ArchRelAppComponentEntity> components = publishingWindowMapper.selectComponentOfDeliver(windowEntity.getProjectMainId(), windowEntity.getId().toString(), null);
            for(ArchRelAppComponentEntity component:components){
                DeliveryArtifactQueryVo queryVo= new DeliveryArtifactQueryVo();
                queryVo.setSystemId(component.getId());
                queryVo.setSysAbb(component.getSysAbb());
                queryVo.setDeliveryId(windowEntity.getId());
                //组件的制品信息
                List<DeliveryTypeAndBuildTypeVO> buildTypeVOS = publishingWindowService.artifactListOfComponentQuery(queryVo);
                if(CollectionUtils.isEmpty(buildTypeVOS)){
                    continue;
                }
                fillParams(buildTypeVOS,allData,component,windowEntity,appEntity,itemReqNumbers);
            }
        }

        //无数据直接返回
        PageResult<BatchAppComponentParVo> pageResult = new PageResult<>();
        pageResult.setCurr(pageVo.getCurr());
        pageResult.setSize(pageVo.getSize());
        if(CollectionUtils.isEmpty(allData)){
            pageResult.setCount(0L);
            return pageResult;
        }

        //筛选数据
        if(CollectionUtils.isNotEmpty(pageVo.getItemReqNumbers())){
            allData=allData.stream().filter(item -> pageVo.getItemReqNumbers().stream()
                    .anyMatch(item.getItemReqNumbers()::contains))
                    .collect(Collectors.toList());
        }
        if(!Objects.isNull(pageVo.getAppId())){
            allData=allData.stream().filter(item -> item.getAppId().equals(pageVo.getAppId()))
                    .collect(Collectors.toList());
        }
        if(StringUtils.isNotEmpty(pageVo.getComponentName())){
            allData=allData.stream().filter(item -> item.getComponentName().contains(pageVo.getComponentName()))
                    .collect(Collectors.toList());
        }
        if(StringUtils.isNotEmpty(pageVo.getDeliverNumber())){
            allData=allData.stream().filter(item -> item.getDeliverNumber().contains(pageVo.getDeliverNumber()))
                    .collect(Collectors.toList());
        }
        if(StringUtils.isNotEmpty(pageVo.getStageName())){
            allData=allData.stream().filter(item -> item.getStageName().equals(pageVo.getStageName()))
                    .collect(Collectors.toList());
        }
        if(!Objects.isNull(pageVo.getLastCICDStatus())){
            if(pageVo.getLastCICDStatus().equals(LastCICDStatusEnums.COVER.getCode())){
                allData=allData.stream().filter(item -> Objects.nonNull(item.getCdPipelineId()) && Objects.nonNull(item.getPipelineId()) && !item.getCdPipelineId().equals(item.getPipelineId()))
                        .collect(Collectors.toList());
            }else if(pageVo.getLastCICDStatus().equals(LastCICDStatusEnums.DONE.getCode())){
                allData=allData.stream().filter(item -> Objects.nonNull(item.getCdPipelineId()) && Objects.nonNull(item.getPipelineId()) && item.getCdPipelineId().equals(item.getPipelineId()))
                        .collect(Collectors.toList());
            }else{
                allData=allData.stream().filter(item -> Objects.isNull(item.getCdPipelineId()))
                        .collect(Collectors.toList());
            }
        }
        if(CollectionUtils.isEmpty(allData)){
            pageResult.setCount(0L);
            return pageResult;
        }
        /*排序规则：
        应用系统名称正序，应用系统相同时按交付版本正序，交付版本相同时，
        按组件名称正序，组件名称相同时按制品类型名称正序。然后按阶段UAT/PP排序。*/
        allData = allData.stream().sorted(Comparator.comparing(BatchArtifactInfoDto::getAppName)
                .thenComparing(BatchArtifactInfoDto::getDeliverNumber)
                .thenComparing(BatchArtifactInfoDto::getComponentName)
                .thenComparing(BatchArtifactInfoDto::getDeliveryTypeDesc)
                .thenComparing(Comparator.comparing(BatchArtifactInfoDto::getStageName).reversed()))
                .collect(Collectors.toList());
        Map<MoreParamGroup, List<BatchArtifactInfoDto>> groupComponetMap = allData.stream().
                collect(Collectors.groupingBy(MoreParamGroup::from));
        List<MoreParamGroup> componentIds = groupComponetMap.keySet().stream().sorted().toList();
        //返回数据
        componentIds = componentIds.stream().skip((pageVo.getCurr() - 1)* pageVo.getSize())
                .limit(pageVo.getSize())
                .collect(Collectors.toList());
        pageResult.setCount((long) groupComponetMap.size());
        pageResult.setData(factoryData(groupComponetMap,componentIds));
        log.info("批次详情-组件与环境-出参：{}", JacksonUtil.obj2String(pageResult));
        return pageResult;
    }

    @Data
    public static class MoreParamGroup implements Comparable<MoreParamGroup>{
        private Long componentId;
        private String deliverNumber;
        private Long appId;

        public MoreParamGroup(Long componentId, String deliverNumber, Long appId) {
            this.componentId = componentId;
            this.deliverNumber = deliverNumber;
            this.appId = appId;
        }

        public static MoreParamGroup from(BatchArtifactInfoDto dto){
            return new MoreParamGroup(dto.getComponentId(),dto.getDeliverNumber(),dto.getAppId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MoreParamGroup that = (MoreParamGroup) o;
            return Objects.equals(componentId, that.componentId) && Objects.equals(deliverNumber, that.deliverNumber) && Objects.equals(appId, that.appId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(componentId, deliverNumber, appId);
        }

        @Override
        public int compareTo(MoreParamGroup o) {
            return Long.compare(this.componentId,o.getComponentId());
        }
    }
    /*
     * 批次关联的应用系统查询
     */
    @Override
    public List<BatchAppVo> batchAppQuery(BatchItemDetailQueryVo deliveryQueryVo) {
        log.info("批次详情-批次关联的应用系统查询-入参：{}", JacksonUtil.obj2String(deliveryQueryVo));
        List<BatchAppVo>  res = new ArrayList<>();
        List<ArchAppEntity> entities = investBatchLinkPasoMapper.selectPasosInfoByBatchId(deliveryQueryVo.getBatchId());
        if(CollectionUtils.isNotEmpty(entities)){
            for(ArchAppEntity e:entities){
                BatchAppVo vo=new BatchAppVo();
                vo.setAppName(e.getCusRequireName());
                vo.setAppId(e.getId());
                res.add(vo);
            }
        }
        log.info("批次详情-批次关联的应用系统查询-出参：{}", JacksonUtil.obj2String(res));
        return res;
    }

    @Override
    public void exportExcel(ComponentEnvQueryPageVo exportVo, HttpServletResponse response) {
        PageResult<BatchAppComponentParVo> pageResult = componentEnvPageQuery(exportVo);
        List<BatchAppComponentParExcelVo> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(pageResult.getData())){
            response.setContentType(ExecutorConstant.OFFICEDOCUMENT_SPREADSHEETML_SHEET);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode(ExecutorConstant.BATCH_DELIVER_FILE_NAME, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader(ExecutorConstant.CONTENT_DISPOSITION, ExecutorConstant.ATTACHMENT_FILENAME_UTF_8 + fileName + ExecutorConstant.EXCEL_SURFIX);
            try {
                EasyExcel.write(response.getOutputStream(), BatchAppComponentParExcelVo.class)
                        .sheet(ExecutorConstant.BATCH_DELIVER_FILE_NAME).doWrite(list);
            } catch (IOException e) {
                log.warn("处理 Excel 异常", e);
                throw new DevopsRuntimeException(DevopsCodeEnums.SYSTEM_ERROR, "处理 Excel 异常");
            }
            return;
        }
        InvestBatchEntity investBatchEntity = investBatchMapper.selectById(exportVo.getBatchId());
        RequireEntity requireEntity = requireMapper.selectById(investBatchEntity.getBizRequirementId());
        StringBuilder cellName=new StringBuilder();
        if(Objects.nonNull(requireEntity)){
            cellName.append(DELIVER_EXPORT_REQ).
                    append(StringUtils.isEmpty(requireEntity.getRequireTitle())?UNDERLINE:requireEntity.getRequireTitle())
                    .append(DELIVER_EXPORT_BLANK);
        }
        cellName.append(DELIVER_EXPORT).
                append(StringUtils.isEmpty(investBatchEntity.getAdpmBatchId())?UNDERLINE:investBatchEntity.getAdpmBatchId())
                .append(DELIVER_EXPORT_BLANK)
                .append(DELIVER_EXPORT_NO).
                append(StringUtils.isEmpty(investBatchEntity.getAdpmBatchNumber())?UNDERLINE:investBatchEntity.getAdpmBatchNumber());
        //填充导出的数据
        for(BatchAppComponentParVo parVo:pageResult.getData()){
            List<BatchAppArtTypeVo> appArtTypeVos = parVo.getAppArtTypeVos();
            for(BatchAppArtTypeVo typeVo:appArtTypeVos){
                for(BatchAppArtifactVo artifactVo:typeVo.getArtifactVos()){
                    BatchAppComponentParExcelVo excelVo=new BatchAppComponentParExcelVo();
                    excelVo.setComponentName(parVo.getComponentName());
                    excelVo.setDeliverNumber(parVo.getDeliverNumber());
                    excelVo.setItemReqNumbers(parVo.getItemReqNumbers());
                    excelVo.setAppName(parVo.getAppName());
                    excelVo.setDeliveryTypeDesc(typeVo.getDeliveryTypeDesc());
                    excelVo.setLockVersion(typeVo.getLockVersion());
                    excelVo.setStageName(artifactVo.getStageName());
                    excelVo.setArtifactName(artifactVo.getArtifactName());
                    excelVo.setArtifactMD5(artifactVo.getArtifactMD5());
                    excelVo.setLastCommitId(artifactVo.getLastCommitId());
                    excelVo.setCpack(artifactVo.getCpack());
                    excelVo.setPipelineId(artifactVo.getPipelineId());
                    excelVo.setCdPipelineId(artifactVo.getCdPipelineId());
                    excelVo.setCdHostList(artifactVo.getCdHostList());
                    list.add(excelVo);
                }
            }
        }
        response.setContentType(ExecutorConstant.OFFICEDOCUMENT_SPREADSHEETML_SHEET);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode(ExecutorConstant.BATCH_DELIVER_FILE_NAME, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader(ExecutorConstant.CONTENT_DISPOSITION, ExecutorConstant.ATTACHMENT_FILENAME_UTF_8 + fileName + ExecutorConstant.EXCEL_SURFIX);
        try {
            EasyExcel.write(response.getOutputStream(), BatchAppComponentParExcelVo.class)
                    .registerWriteHandler(new ReportSheetHandler(cellName.toString()))
                    .relativeHeadRowIndex(2)
                    .sheet(ExecutorConstant.BATCH_DELIVER_FILE_NAME).doWrite(list);
        } catch (IOException e) {
            log.warn("处理 Excel 异常", e);
            throw new DevopsRuntimeException(DevopsCodeEnums.SYSTEM_ERROR, "处理 Excel 异常");
        }
    }

    /**
     * 组装处理数据
     */
    private List<BatchAppComponentParVo> factoryData(Map<MoreParamGroup, List<BatchArtifactInfoDto>> groupComponetMap,List<MoreParamGroup> componentIds) {
        List<BatchAppComponentParVo> componentVos = new ArrayList<>();
        //按照组件分页后的所有制品信息
        for(MoreParamGroup componentEntry:componentIds){
            List<BatchArtifactInfoDto> componentAllData=groupComponetMap.get(componentEntry);
            BatchAppComponentParVo componentVo= new BatchAppComponentParVo();
            componentVo.setComponentId(componentEntry.getComponentId());
            componentVo.setComponentName(componentAllData.get(0).getComponentName());
            componentVo.setItemReqNumbers(componentAllData.get(0).getItemReqNumbers());
            componentVo.setDeliverNumber(componentEntry.getDeliverNumber());
            componentVo.setAppId(componentEntry.getAppId());
            componentVo.setAppName(componentAllData.get(0).getAppName());
            Map<String, List<BatchArtifactInfoDto>> deliveryTypeMap = componentAllData.stream().
                    collect(Collectors.groupingBy(BatchArtifactInfoDto::getDeliveryTypeDesc));
            List<BatchAppArtTypeVo> appArtTypeVos = new ArrayList<>();
            //合并制品类型
            for(Map.Entry<String,List<BatchArtifactInfoDto>> deliveryTypeEntry: deliveryTypeMap.entrySet()){
                List<BatchArtifactInfoDto> typeAllData=deliveryTypeEntry.getValue();
                BatchAppArtTypeVo typeVo = new BatchAppArtTypeVo();
                typeVo.setLockVersion(typeAllData.get(0).getLockVersion());
                typeVo.setDeliveryTypeDesc(deliveryTypeEntry.getKey());
                typeVo.setArtifactVos(IGenerator.convert(typeAllData,BatchAppArtifactVo.class));
                appArtTypeVos.add(typeVo);
            }
            componentVo.setAppArtTypeVos(appArtTypeVos);
            componentVos.add(componentVo);
        }


        return componentVos;
    }

    //组装返回数据
    /*private List<BatchAppVo> factoryData(Map<Long, List<BatchArtifactInfoDto>> groupComponetMap,List<Long> componentIds) {
        List<BatchAppVo> resVos = new ArrayList<>();
        //按照组件分页后的所有制品信息
        List<BatchArtifactInfoDto> resAllData=new ArrayList<>();
        for(Long id:componentIds){
            resAllData.addAll(groupComponetMap.get(id));
        }
        Map<Long, List<BatchArtifactInfoDto>> appMap = resAllData.stream().
                collect(Collectors.groupingBy(BatchArtifactInfoDto::getAppId));
        //合并应用系统
        for(Map.Entry<Long,List<BatchArtifactInfoDto>> appEntry: appMap.entrySet()){
            List<BatchArtifactInfoDto> appAllData = appEntry.getValue();
            BatchAppVo appVo=new BatchAppVo();
            appVo.setAppId(appAllData.get(0).getAppId());
            appVo.setAppName(appAllData.get(0).getAppName());
            Map<String, List<BatchArtifactInfoDto>> deliverMap = appAllData.stream().
                    collect(Collectors.groupingBy(BatchArtifactInfoDto::getDeliverNumber));
            List<BatchAppDeliverVo> deliverVos = new ArrayList<>();
            //合并交付版本
            for(Map.Entry<String,List<BatchArtifactInfoDto>> deliverEntry: deliverMap.entrySet()){
                List<BatchArtifactInfoDto> deliverAllData = deliverEntry.getValue();
                BatchAppDeliverVo deliverVo = new BatchAppDeliverVo();
                deliverVo.setDeliverNumber(deliverEntry.getKey());
                deliverVo.setItemReqNumbers(deliverAllData.get(0).getItemReqNumbers());
                Map<Long, List<BatchArtifactInfoDto>> componentMap = deliverAllData.stream().
                        collect(Collectors.groupingBy(BatchArtifactInfoDto::getComponentId));
                List<BatchAppComponentVo> componentVos = new ArrayList<>();
                //合并组件
                for(Map.Entry<Long,List<BatchArtifactInfoDto>> componentEntry: componentMap.entrySet()){
                    List<BatchArtifactInfoDto> componentAllData=componentEntry.getValue();
                    BatchAppComponentVo componentVo= new BatchAppComponentVo();
                    componentVo.setComponentId(componentEntry.getKey());
                    componentVo.setComponentName(componentAllData.get(0).getComponentName());
                    Map<String, List<BatchArtifactInfoDto>> deliveryTypeMap = componentAllData.stream().
                            collect(Collectors.groupingBy(BatchArtifactInfoDto::getDeliveryTypeDesc));
                    List<BatchAppArtTypeVo> appArtTypeVos = new ArrayList<>();
                    //合并制品类型
                    for(Map.Entry<String,List<BatchArtifactInfoDto>> deliveryTypeEntry: deliveryTypeMap.entrySet()){
                        List<BatchArtifactInfoDto> typeAllData=deliveryTypeEntry.getValue();
                        BatchAppArtTypeVo typeVo = new BatchAppArtTypeVo();
                        typeVo.setLockVersion(typeAllData.get(0).getLockVersion());
                        typeVo.setDeliveryTypeDesc(deliveryTypeEntry.getKey());
                        typeVo.setArtifactVos(IGenerator.convert(typeAllData,BatchAppArtifactVo.class));
                        appArtTypeVos.add(typeVo);
                    }
                    componentVo.setAppArtTypeVos(appArtTypeVos);
                    componentVos.add(componentVo);
                }
                deliverVo.setComponentVos(componentVos);
                deliverVos.add(deliverVo);
            }
            appVo.setDeliverVos(deliverVos);
            resVos.add(appVo);
        }
        return resVos;
    }*/

    //填充制品vo的属性
    private boolean fillParams(List<DeliveryTypeAndBuildTypeVO> buildTypeVOS,List<BatchArtifactInfoDto> allData,
             ArchRelAppComponentEntity component, PublishingWindowEntity windowEntity,ArchAppEntity appEntity,List<String> itemReqNumbers) {
        for(DeliveryTypeAndBuildTypeVO typeVO:buildTypeVOS){
            for (DeliveryVersionArtifactInfoVo artifactVo:typeVO.getArtifactInfoList()) {
                BatchArtifactInfoDto vo = IGenerator.convert(artifactVo, BatchArtifactInfoDto.class);
                //应用于组件信息
                vo.setComponentId(component.getId());
                vo.setComponentName(component.getSysAbb());
                vo.setDeliverNumber(windowEntity.getBatchNumber());
                vo.setItemReqNumbers(itemReqNumbers);
                vo.setAppName(Objects.isNull(appEntity)?"":appEntity.getCusRequireName());
                vo.setAppId(Objects.isNull(appEntity)?null:appEntity.getId());
                //制品类型信息
                vo.setDeliveryTypeDesc(typeVO.getDeliveryTypeDesc());
                vo.setLockVersion(typeVO.getLockVersion());
                allData.add(vo);
            }
        }
        return true;
    }


}
