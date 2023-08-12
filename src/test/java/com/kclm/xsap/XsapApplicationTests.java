package com.kclm.xsap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kclm.xsap.dao.CourseCardDao;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.TimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class XsapApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private MemberDao dao;

    @Autowired
    private MemberCardService cardService;

    @Autowired
    private MemberCardDao cardDao;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private CourseCardDao courseCardDao;

    @Autowired
    private MemberService memberService;

    @Test
    public void testSelect() {
        List<MemberDTO> dtoList = dao.memberEntiesList();
        for (MemberDTO d :
                dtoList) {
            System.out.println(d);
        }
    }

    @Test
    public void testTimePlus() {
        LocalDateTime now = LocalDateTime.now();
        Integer day = 10;
        LocalDateTime localDateTime = TimeUtil.timeSub(now, day);
        System.out.println(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Test
    public void testRegex() {
        String regex = "^1([3456789])\\d{9}$";
        String phone = "15261570017";
        boolean b = phone.matches(regex);
        System.out.println(b);
    }

    @Test
    public void testQueryWrapper() {
        QueryWrapper<MemberCardEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");
        List<MemberCardEntity> cardEntityList = cardService.list(queryWrapper);
        System.out.println(cardEntityList);
        List<Long> cardIdList = cardDao.getCardIdList();
        System.out.println(cardIdList);
    }

    @Test
    public void testDate() {
        Long start = 1690646400L;
        Long end = 1694275200L;
        Instant instant = Instant.ofEpochMilli(start);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        Instant instant1 = Instant.ofEpochMilli(end);
        LocalDateTime localDateTime1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());

        System.out.println(localDateTime);
        System.out.println(localDateTime1);
    }

    @Test
    public void testAddAndLost() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 获取本月第一天
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        // 获取本月最后一天
        int lastDay = now.getMonth().length(now.toLocalDate().isLeapYear());
        int dayOfMonth = now.getDayOfMonth();
        LocalDateTime lastSecondOfToday = now.withDayOfMonth(dayOfMonth).toLocalDate().atTime(23, 59, 59);

        List<MemberEntity> memberEntityList = memberService.filterAllMemberByTime(firstDayOfMonth, lastSecondOfToday);
        List<Integer> dataArr = new ArrayList<>();
        for (int i = 1; i <= dayOfMonth; i++) {
            dataArr.add(0);
        }

        List<Integer> data = dataArr;
        List<Integer> data2 = dataArr;

        data = memberEntityList.stream().filter(item -> item.getIsDeleted() == 1).map(item -> {
            int day = item.getCreateTime().getDayOfMonth();
            dataArr.set(day - 1, dataArr.get(day) + 1);
            return dataArr;
        }).collect(Collectors.toList()).get(0);
        System.out.println(Arrays.toString(data.toArray()));
    }

}
