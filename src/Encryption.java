package org.toocans.broker.api;

import cn.hutool.json.JSONUtil;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * OpenAPI 接口调用示例
 */
public class Encryption {
//    final static String API_KEY = "MCowBQYDK2VwAyEAXmkBih05iCSED2FWQGhHfRKvCEdPvUxKODoThg9K0KA";
//    final static String API_SECRET = "MC4CAQAwBQYDK2VwBCIEILgovvekSbdI9uLp/TJKJO8mgn9TynZLT+q4EyQR";

//
    final static String API_KEY = "MCowBQYDK2VwAyEAnW/xONd55t9M/mOv/y/VSiJumoMWOew+Tr8UKbuPG/o=";
    final static String API_SECRET = "MC4CAQAwBQYDK2VwBCIEIPOciC7O+zn84xhZmmbuVa8KUJ/kCgJb8YImeizZF0zr";




    final  static String TIMESTAMP = System.currentTimeMillis() +"";;
    final static String RECV_WINDOW = "20000";
//    final static String BASE_U= "http://localhost:8082";
    final static String BASE_U= "https://test-api.bdy.tech";
    final static String BASE_URL = "/t-api/toocans-broker-api/v1/op/openapi";

    public static void main(String[] args) throws Exception {

        Encryption encryptionTest = new Encryption();

        // 创建子用户
        encryptionTest.createSubUser();
        
//        // 查询资产
        encryptionTest.queryBrokerAssets("");
//
//        // 获取充值地址
        encryptionTest.getDepositAddress(675861471654133738L, "TBSC_BNB");
//
//        // 查询充值订单
        long subUid = 468001460825249908L;
        encryptionTest.queryDepositOrders(subUid, "USDT", 1, 10, null, null,null);
//
//        // 创建提币订单
        String clientOrderId = UUID.randomUUID().toString().replace("-", "");
        encryptionTest.createWithdrawal(449267154888989985L, "TBSC_BNB", "0.001045", "0x2c90a96735d851c6728fb6949264b88198b5dc6c", clientOrderId);

//        // 查询提币订单
        encryptionTest.queryWithdrawalOrders(subUid, "USDT", 1, 20, null, null);

        // 查询提币订单详情
        String  orederId = "b15d4321e38046c19955dac55c9043f2";
        encryptionTest.withdrawalOrderInfo(orederId);
    }

    /**
     * 创建子用户
     */
    public void createSubUser() throws Exception {
        Map<String, Object> map = new HashMap<>();
        
        String signature = genPostSign(map,BASE_URL + "/createSubUser");
        String jsonMap = "";

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/createSubUser")
                .post(RequestBody.create(mediaType, jsonMap))
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("创建子用户响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 查询Broker资产
     * @param tokenId 币种ID (可选)
     */
    public void queryBrokerAssets(String tokenId)  throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (tokenId != null) {
            map.put("tokenId", tokenId);
        }

        String signature = genGetSign(map,BASE_URL + "/assets");
        StringBuilder sb = genQueryStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/assets?" + sb)
                .get()
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("查询资产响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取充值地址
     * @param subUid 子用户ID
     * @param tokenId 币种ID
     */
    public void getDepositAddress(Long subUid, String tokenId) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("subUid", subUid);
        map.put("tokenId", tokenId);
        
        String signature = genPostSign(map,BASE_URL + "/depositAddress");
        String jsonMap =JSONUtil.toJsonStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/depositAddress")
                .post(RequestBody.create(mediaType, jsonMap))
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("获取充值地址响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 查询充值订单
     * @param subUid 子用户ID
     * @param tokenId 币种ID (可选)
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param startTime 开始时间 (可选)
     * @param endTime 结束时间 (可选)
     */
    public void queryDepositOrders(Long subUid, String tokenId, Integer pageNo, Integer pageSize, 
                                  Long startTime, Long endTime,String address) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("subUid", subUid);
        map.put("pageNo", pageNo);
        map.put("pageSize", pageSize);
        
        if (tokenId != null) {
            map.put("tokenId", tokenId);
        }
        if (startTime != null) {
            map.put("startTime", startTime);
        }
        if (endTime != null) {
            map.put("endTime", endTime);
        }
        if (address != null) {
            map.put("walletAddress", address);
        }


        
        String signature = genPostSign(map,BASE_URL + "/depositOrders");
        String jsonMap = JSONUtil.toJsonStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/depositOrders")
                .post(RequestBody.create(mediaType, jsonMap))
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("查询充值订单响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建提币订单
     * @param subUid 子用户ID
     * @param tokenId 币种ID
     * @param amount 提币金额
     * @param address 提币地址
     * @param clientOrderId 客户端订单ID (可选)
     */
    public void createWithdrawal(Long subUid, String tokenId, String amount, String address, 
                               String clientOrderId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("subUid", subUid);
        map.put("tokenId", tokenId);
        map.put("amount", amount);
        map.put("address", address);
        
        if (clientOrderId != null) {
            map.put("clientWithdrawalId", clientOrderId);
        }
        
        String signature = genPostSign(map,BASE_URL + "/createWithdrawal");
        String jsonMap = JSONUtil.toJsonStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/createWithdrawal")
                .post(RequestBody.create(mediaType, jsonMap))
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("创建提币订单响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 查询提币订单
     * @param subUid 子用户ID
     * @param tokenId 币种ID (可选)
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param startTime 开始时间 (可选)
     * @param endTime 结束时间 (可选)
     */
    public void queryWithdrawalOrders(Long subUid, String tokenId, Integer pageNo, Integer pageSize, 
                                    Long startTime, Long endTime) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("subUid", subUid);
        map.put("pageNo", pageNo);
        map.put("pageSize", pageSize);
        
        if (tokenId != null) {
            map.put("tokenId", tokenId);
        }
        if (startTime != null) {
            map.put("startTime", startTime);
        }
        if (endTime != null) {
            map.put("endTime", endTime);
        }
        
        String signature = genPostSign(map,BASE_URL + "/withdrawalOrders");
        String jsonMap = JSONUtil.toJsonStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(BASE_U+BASE_URL + "/withdrawalOrders")
                .post(RequestBody.create(mediaType, jsonMap))
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("查询提币订单响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 查询提币订单详情
     * @param clientWithdrawalId 客户端提币订单ID
     */
    public void withdrawalOrderInfo(String clientWithdrawalId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("clientWithdrawalId", clientWithdrawalId);
        
        String signature = genGetSign(map, BASE_URL + "/withdrawalOrderInfo");
        StringBuilder sb = genQueryStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(BASE_U + BASE_URL + "/withdrawalOrderInfo?" + sb)
                .get()
                .addHeader("TOOCANS-ACCESS-KEY", API_KEY)
                .addHeader("TOOCANS-ACCESS-SIGN", signature)
                .addHeader("TOOCANS-ACCESS-TIMESTAMP", TIMESTAMP)
                .addHeader("TOOCANS-ACCESS-RECV-WINDOW", RECV_WINDOW)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println("查询提币订单详情响应: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The way to generate the sign for POST requests
     * @param params: Map input parameters
     * @return signature used to be a parameter in the header
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static String genPostSign(Map<String, Object> params, String requestPath) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        String paramJson = JSONUtil.toJsonStr(params);

        
        String signContent = TIMESTAMP + "POST" + RECV_WINDOW + requestPath;
        if (paramJson != null && !paramJson.equals("{}")) {
            signContent += paramJson;
        }

        System.out.println(signContent);
        return generateHmacSha256Signature(signContent, API_SECRET);
    }

    /**
     * The way to generate the sign for GET requests
     * @param params: Map input parameters
     * @return signature used to be a parameter in the header
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static String genGetSign(Map<String, Object> params, String path) throws Exception{
        StringBuilder queryStr = genQueryStr(params);
        String requestPath = path+"";
        if (queryStr.length() > 0) {
            requestPath += "?" + queryStr;
        }
        
        String signContent = TIMESTAMP + "GET" + RECV_WINDOW + requestPath;
        System.out.println(signContent);
        return generateHmacSha256Signature(signContent, API_SECRET);
    }

    private static String generateHmacSha256Signature(String data, String secret) throws Exception{

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

    }

    /**
     * To generate query string for GET requests
     * @param map
     * @return
     */
    private static StringBuilder genQueryStr(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            String key = iter.next();
            sb.append(key)
                    .append("=")
                    .append(map.get(key))
                    .append("&");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }
}
