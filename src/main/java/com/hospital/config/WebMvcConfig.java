package com.hospital.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Cấu hình Web MVC để ánh xạ thư mục lưu trữ file vật lý thành URL tĩnh.
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadFolder = "uploads";
        File file = new File(uploadFolder);
        String absolutePath = file.getAbsolutePath();
        
        log.info("Khởi tạo cấu hình ánh xạ tài nguyên tĩnh.");
        log.info("Đường dẫn vật lý của thư mục lưu file: {}", absolutePath);

        // 2. Thực hiện đăng ký bộ ánh xạ (Resource Handler)
        // - addResourceHandler("/uploads/**"): Mọi URL bắt đầu bằng /uploads/ sẽ được kích hoạt bộ xử lý này
        // - addResourceLocations("file:" + ...): Chỉ định vị trí lưu trữ file vật lý tương ứng trên ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
                
        log.info("Đã cấu hình thành công: Đăng ký URL tĩnh [/uploads/**] gán với thư mục vật lý [file:{}{}]", 
                absolutePath, File.separator);
    }
}
