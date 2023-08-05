package com.kclm.xsap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kclm.xsap.dao.CourseCardDao;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.TimeCalculate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private CourseCardDao courseCardDao;

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
        LocalDateTime localDateTime = TimeCalculate.timeSub(now, day);
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
    public void testDelete() {
        Long id = 49L;
        courseCardDao.deleteCourseCard(id);
    }

}
