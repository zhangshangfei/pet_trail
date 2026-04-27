package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.mapper.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final ReportMapper reportMapper;
    private final AdminOperationLogMapper logMapper;
    private final PetMapper petMapper;
    private final ChallengeMapper challengeMapper;
    private final ProductMapper productMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportUsers(HttpServletResponse response, String keyword, Integer status) throws IOException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getOpenid, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        List<User> users = userMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("用户数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "昵称", "OpenID", "性别", "状态", "注册时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (User u : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.getId());
                row.createCell(1).setCellValue(u.getNickname() != null ? u.getNickname() : "");
                row.createCell(2).setCellValue(u.getOpenid() != null ? u.getOpenid() : "");
                row.createCell(3).setCellValue(u.getGender() != null ? (u.getGender() == 1 ? "男" : u.getGender() == 2 ? "女" : "未知") : "未知");
                row.createCell(4).setCellValue(u.getStatus() != null ? (u.getStatus() == 1 ? "正常" : "禁用") : "未知");
                row.createCell(5).setCellValue(u.getCreatedAt() != null ? u.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "users");
        }
    }

    public void exportPosts(HttpServletResponse response, Integer auditStatus) throws IOException {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(Post::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        List<Post> posts = postMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("动态数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "用户ID", "内容", "审核状态", "点赞数", "评论数", "是否删除", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Post p : posts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getUserId() != null ? p.getUserId() : 0);
                String content = p.getContent();
                row.createCell(2).setCellValue(content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : "");
                row.createCell(3).setCellValue(p.getAuditStatus() != null ? switch (p.getAuditStatus()) { case 1 -> "已通过"; case 2 -> "已拒绝"; default -> "待审核"; } : "待审核");
                row.createCell(4).setCellValue(p.getLikeCount() != null ? p.getLikeCount() : 0);
                row.createCell(5).setCellValue(p.getCommentCount() != null ? p.getCommentCount() : 0);
                row.createCell(6).setCellValue(p.getDeleted() != null && p.getDeleted() == 1 ? "是" : "否");
                row.createCell(7).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "posts");
        }
    }

    public void exportReports(HttpServletResponse response, Integer status) throws IOException {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        List<Report> reports = reportMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("举报数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "举报用户ID", "目标类型", "目标ID", "原因", "状态", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Report r : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getReporterId() != null ? r.getReporterId() : 0);
                row.createCell(2).setCellValue(r.getTargetType() != null ? r.getTargetType() : "");
                row.createCell(3).setCellValue(r.getTargetId() != null ? r.getTargetId() : 0);
                row.createCell(4).setCellValue(r.getReason() != null ? r.getReason() : "");
                row.createCell(5).setCellValue(r.getStatus() != null ? switch (r.getStatus()) { case 1 -> "已处理"; case 2 -> "已驳回"; default -> "待处理"; } : "待处理");
                row.createCell(6).setCellValue(r.getCreatedAt() != null ? r.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "reports");
        }
    }

    public void exportLogs(HttpServletResponse response, String module) throws IOException {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AdminOperationLog::getModule, module);
        }
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);
        List<AdminOperationLog> logs = logMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("操作日志");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "管理员", "模块", "操作", "详情", "IP", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (AdminOperationLog l : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getId());
                row.createCell(1).setCellValue(l.getAdminName() != null ? l.getAdminName() : "");
                row.createCell(2).setCellValue(l.getModule() != null ? l.getModule() : "");
                row.createCell(3).setCellValue(l.getAction() != null ? l.getAction() : "");
                row.createCell(4).setCellValue(l.getDetail() != null ? l.getDetail() : "");
                row.createCell(5).setCellValue(l.getIp() != null ? l.getIp() : "");
                row.createCell(6).setCellValue(l.getCreatedAt() != null ? l.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "logs");
        }
    }

    public void exportPets(HttpServletResponse response, Integer category) throws IOException {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(Pet::getCategory, category);
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        List<Pet> pets = petMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("宠物数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "用户ID", "名称", "品种", "类别", "性别", "体重(kg)", "绝育", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Pet p : pets) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getUserId() != null ? p.getUserId() : 0);
                row.createCell(2).setCellValue(p.getName() != null ? p.getName() : "");
                row.createCell(3).setCellValue(p.getBreed() != null ? p.getBreed() : "");
                row.createCell(4).setCellValue(p.getCategory() != null ? (p.getCategory() == 1 ? "猫" : p.getCategory() == 2 ? "狗" : "其他") : "未知");
                row.createCell(5).setCellValue(p.getGender() != null ? (p.getGender() == 1 ? "公" : p.getGender() == 2 ? "母" : "未知") : "未知");
                row.createCell(6).setCellValue(p.getWeight() != null ? p.getWeight().doubleValue() : 0);
                row.createCell(7).setCellValue(p.getSterilized() != null ? (p.getSterilized() == 1 ? "已绝育" : "未绝育") : "未知");
                row.createCell(8).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "pets");
        }
    }

    public void exportChallenges(HttpServletResponse response, Integer status) throws IOException {
        LambdaQueryWrapper<Challenge> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Challenge::getStatus, status);
        }
        wrapper.orderByDesc(Challenge::getCreatedAt);
        List<Challenge> challenges = challengeMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("挑战数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "标题", "描述", "状态", "开始时间", "结束时间", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Challenge c : challenges) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getTitle() != null ? c.getTitle() : "");
                row.createCell(2).setCellValue(c.getDescription() != null ? c.getDescription() : "");
                row.createCell(3).setCellValue(c.getStatus() != null ? (c.getStatus() == 1 ? "进行中" : c.getStatus() == 2 ? "已结束" : "未开始") : "未知");
                row.createCell(4).setCellValue(c.getStartDate() != null ? c.getStartDate().format(DTF) : "");
                row.createCell(5).setCellValue(c.getEndDate() != null ? c.getEndDate().format(DTF) : "");
                row.createCell(6).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "challenges");
        }
    }

    public void exportProducts(HttpServletResponse response, Integer status) throws IOException {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        List<Product> products = productMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("商品数据");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "名称", "分类", "品牌", "价格", "原价", "销量", "评分", "状态", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName() != null ? p.getName() : "");
                row.createCell(2).setCellValue(p.getCategory() != null ? p.getCategory() : "");
                row.createCell(3).setCellValue(p.getBrand() != null ? p.getBrand() : "");
                row.createCell(4).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(p.getOriginalPrice() != null ? p.getOriginalPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(p.getSalesCount() != null ? p.getSalesCount() : 0);
                row.createCell(7).setCellValue(p.getRating() != null ? p.getRating().doubleValue() : 0);
                row.createCell(8).setCellValue(p.getStatus() != null ? (p.getStatus() == 1 ? "上架" : "下架") : "未知");
                row.createCell(9).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(DTF) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            writeResponse(response, wb, "products");
        }
    }

    private void writeResponse(HttpServletResponse response, Workbook wb, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
        wb.write(response.getOutputStream());
    }
}
