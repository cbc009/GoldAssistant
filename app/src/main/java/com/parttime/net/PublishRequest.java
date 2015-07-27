package com.parttime.net;

import com.android.volley.RequestQueue;
import com.parttime.pojo.PartJob;
import com.parttime.publish.vo.JobManageListVo;
import com.parttime.publish.vo.PublishActivityListVo;
import com.parttime.utils.ApplicationUtils;
import com.parttime.utils.CheckUtils;
import com.quark.common.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 发布相关接口
 * Created by wyw on 2015/7/25.
 */
public class PublishRequest extends BaseRequest {

    public final static int PUBLISH_ACTIVITY_LIST_TYPE_RECRUIT = 1;
    public final static int PUBLISH_ACTIVITY_LIST_TYPE_AUDITING = 2;
    public final static int PUBLISH_ACTIVITY_LIST_TYPE_UNDERCARRIAGE = 3;

    public void publish(PartJob partJob, RequestQueue requestQueue, DefaultCallback callback) {
        HashMap<String, String> reqParams = new HashMap<>();
        reqParams.put("company_id", String.valueOf(partJob.companyId));
        reqParams.put("type", partJob.type);
        reqParams.put("title", partJob.title);
        reqParams.put("start_time", partJob.beginTime);
        reqParams.put("end_time", partJob.endTime);
        reqParams.put("city", partJob.city);
        reqParams.put("county", partJob.area);
        reqParams.put("address", partJob.address);
        reqParams.put("pay", String.valueOf(partJob.salary));
        reqParams.put("pay_type", String.valueOf(partJob.salaryUnit.ordinal()));
        reqParams.put("pay_form", partJob.payType);
        int apartSexInt = partJob.apartSex ? 1 : 0;
        reqParams.put("apart_sex", String.valueOf(apartSexInt));
        if (partJob.apartSex) {
            reqParams.put("male_count", String.valueOf(partJob.maleNum));
            reqParams.put("female_count", String.valueOf(partJob.femaleNum));
        } else {
            reqParams.put("head_count", String.valueOf(partJob.headSum));
        }
        reqParams.put("require_info", partJob.workRequire);
        int isShowTelInt = partJob.isShowTel ? 1 : 0;
        reqParams.put("show_telephone", String.valueOf(isShowTelInt));
        if (partJob.isHasMoreRequire()) {
            if (partJob.height != null) {
                reqParams.put("require_height", String.valueOf(partJob.height));
            }
            if (partJob.isHasMeasurements()) {
                reqParams.put("require_bust", String.valueOf(partJob.bust));
                reqParams.put("require_beltline", String.valueOf(partJob.beltline));
                reqParams.put("require_hipline", String.valueOf(partJob.hipline));
            }
            if (partJob.healthProve != null) {
                int healthProveInt = partJob.healthProve ? 1 : 0;
                reqParams.put("require_health_rec", String.valueOf(healthProveInt));
            }
            if (CheckUtils.isEmpty(partJob.language)) {
                reqParams.put("require_language", partJob.language);
            }
        }

//        String url = Url.COMPANY_publish + "?token=" + MainTabActivity.token;
        String url = Url.COMPANY_publish;

        request(url, reqParams, requestQueue, callback);
    }

    /**
     * 已发布活动列表
     *
     * @param page  页码号
     * @param count 分页大小
     * @param type  类型（1-招人中，2-审核中，3已下架）
     */
    public void publishActivityList(int page, int count, int type,
                                    RequestQueue requestQueue, final DefaultCallback callback) {
        HashMap<String, String> reqParams = new HashMap<>();
        reqParams.put("company_id", String.valueOf(ApplicationUtils.getLoginId()));
        reqParams.put("pn", String.valueOf(page));
        reqParams.put("page_size", String.valueOf(count));
        reqParams.put("type", String.valueOf(type));

        String url = Url.COMPANY_MyJianzhi_List;
        request(url, reqParams, requestQueue, new Callback() {
            @Override
            public void success(Object obj) throws JSONException {
                JSONObject jsonObject = (JSONObject) obj;
                JSONObject activityPage = jsonObject.getJSONObject("activityPage");
                PublishActivityListVo publishActivityListVo = new PublishActivityListVo();
                publishActivityListVo.pageNumber = activityPage.getInt("pageNumber");
                publishActivityListVo.pageSize = activityPage.getInt("pageSize");
                publishActivityListVo.totlePage = activityPage.getInt("totalPage");
                publishActivityListVo.totleRow = activityPage.getInt("totalRow");
                JSONArray list = activityPage.getJSONArray("list");
                List<JobManageListVo> jobManageListVoList = new ArrayList<>();

                if (list != null) {
                    for (int i = 0; i < list.length(); ++i) {
                        JSONObject listItem = list.getJSONObject(i);
                        JobManageListVo jobManageListVo = new JobManageListVo();
                        jobManageListVo.jobId = listItem.getInt("activity_id");
                        jobManageListVo.jobTitle = listItem.getString("title");
                        jobManageListVo.view = listItem.getInt("view_count");
                        jobManageListVo.hire = listItem.getInt("confirmed_count");
                        jobManageListVoList.add(jobManageListVo);
                    }
                }

                publishActivityListVo.jobManageListVoList = jobManageListVoList;

                callback.success(publishActivityListVo);
            }

            @Override
            public void failed(Object obj) {
                callback.failed(obj);
            }
        });
    }
}
