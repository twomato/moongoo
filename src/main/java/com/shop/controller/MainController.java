package com.shop.controller;


import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {
    private  final ItemService itemService;
    @GetMapping(value ="/")
    public String main(ItemSearchDto itemSearchDto, Model model) {
        // itemSearchDto가 null인지 확인
        if (itemSearchDto == null) {
            return "error"; // 적절한 에러 페이지로 리다이렉트
        }

        String itemDetail = itemSearchDto.getSearchQuery();

        if (itemDetail != null && !itemDetail.trim().isEmpty()) {
            // Use the service to fetch items based on the search query
            List<Item> items = itemService.searchItems(itemDetail);
            model.addAttribute("items", items);
        } else {
            // 검색 쿼리가 비어 있는 경우 처리
            model.addAttribute("message", "검색어를 입력해 주세요.");
        }

        return "main"; // Adjust to the name of your view template
    }
    @GetMapping("/main/list")
    public ResponseEntity<List<MainItemDto>> main(ItemSearchDto itemSearchDto,
                                                  @RequestParam("offset") int offset,
                                                  @RequestParam("limit") int limit,
                                                  @RequestParam(value = "category", required = false) String category) {
        System.out.println(category);
        //itemSearchDto.setCategory(category); // 카테고리 필터링을 위해 ItemSearchDto 에 설정
        List<MainItemDto> items;
        if(category.isEmpty()){
            items = itemService.getMainItemPage(itemSearchDto, offset, limit);
        }
        else{
            items = itemService.getCategoryItemPage(category, offset, limit);
        }

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/recommend")
    public String recommend(Model model) {

        return "/recommend"; // map.html 템플릿 뷰를 반환
    }

}
