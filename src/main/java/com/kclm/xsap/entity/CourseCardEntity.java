package com.kclm.xsap.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 中间表：课程-会员卡
 *
 * @author fangkai
 * @email ing@163.com
 * @date 2021-12-04 16:18:21
 */
@Data
@TableName("t_course_card")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourseCardEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 会员卡id
	 */
	@TableId
	private Long cardId;
	/**
	 * 课程id
	 */
	private Long courseId;

}
