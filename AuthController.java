package jp.co.internous.crocus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.crocus.model.domain.MstUser;
import jp.co.internous.crocus.model.form.UserForm;
import jp.co.internous.crocus.model.mapper.MstUserMapper;
import jp.co.internous.crocus.model.mapper.TblCartMapper;
import jp.co.internous.crocus.model.session.LoginSession;

@RestController
@RequestMapping(value = "/crocus/auth")
public class AuthController {
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private LoginSession session;
	
	@Autowired
	private Gson gson = new Gson();
	
	@Autowired
	private TblCartMapper cartMapper;
	
	@RequestMapping(value = "/login")
	public String login(@RequestBody UserForm form) {
		
		MstUser user = userMapper.findByUserNameAndPassword(form.getUserName(), form.getPassword());
		
		int tmpUserId = session.getTmpUserId();
		
		if (user != null) {
			int count = cartMapper.findCountByUserId(tmpUserId);
			if (count > 0) {
				cartMapper.updateUserId(user.getId(), tmpUserId);
			}
			
			session.setUserId(user.getId());
			session.setTmpUserId(0);
			session.setUserName(user.getUserName());
			session.setPassword(user.getPassword());
			session.setLogined(true);
		} else {
			session.setUserId(0);
			session.setTmpUserId(0);
			session.setUserName(null);
			session.setPassword(null);
			session.setLogined(false);
		}
		return gson.toJson(user);
	}
	
	@RequestMapping("/logout")
	public String logout() {
		session.setUserId(0);
		session.setTmpUserId(0);
		session.setUserName(null);
		session.setPassword(null);
		session.setLogined(false);
		return "";
	}
	
	@RequestMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm form) {
		String newPassword = form.getNewPassword();
		String cfmPassword = form.getNewPasswordConfirm();
		String message = "パスワードが再設定されました。";
		
		MstUser user = userMapper.findByUserNameAndPassword(form.getUserName(), form.getPassword());
		
		if (user == null) {
			message = "現在のパスワードが正しくありません。";
		} else if (user.getPassword().equals(newPassword)) {
			message = "現在のパスワードと同一文字列が入力されました。";
		} else if (!(newPassword.equals(cfmPassword))) {
			message = "新パスワードと確認用パスワードが一致しません。";
		} else {
			userMapper.updatePassword(user.getUserName(), form.getNewPassword());
			session.setPassword(form.getNewPassword());
		}
		return message;
	}
}
