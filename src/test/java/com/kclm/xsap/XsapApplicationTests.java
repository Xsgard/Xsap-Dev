package com.kclm.xsap;

import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dto.MemberDTO;
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

}
