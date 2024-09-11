package com.shop.service;

import com.shop.dto.*;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;

    public List<ItemDto> getItems(int offset, int limit, String sortBy) {
        Pageable pageable;
        switch (sortBy) {
            case "name":
                pageable = PageRequest.of(offset / limit, limit, Sort.by("itemNm").ascending());
                break;
            case "priceDesc":
                pageable = PageRequest.of(offset / limit, limit, Sort.by("price").descending());
                break;
            case "priceAsc":
                pageable = PageRequest.of(offset / limit, limit, Sort.by("price").ascending());
                break;
            case "newest":
            default:
                pageable = PageRequest.of(offset / limit, limit, Sort.by("createdDate").descending());
                break;
        }

        Page<Item> itemPage = itemRepository.findAll(pageable);

        return itemPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ItemDto convertToDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setItemNm(item.getItemNm());
        itemDto.setPrice(item.getPrice());
        itemDto.setItemDetail(item.getItemDetail());
        itemDto.setSellStatCd(item.getItemSellStatus().name()); // ItemSellStatus를 String으로 변환
        itemDto.setRegTime(item.getRegTime()); // BaseEntity에서 상속받은 regTime
        itemDto.setUpdateTime(item.getUpdateTime()); // BaseEntity에서 상속받은 updateTime
        return itemDto;
    }


    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList)
            throws Exception{
        //상품 등록
        Item item = itemFormDto.createItem();
        item.setItemCategory(itemFormDto.getItemCategory()); // 카테고리 설정
        itemRepository.save(item);
        //이미지 등록
        for (int i=0; i<itemImgFileList.size(); i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            if (i==0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");
            itemImgService.saveItemImg(itemImg,itemImgFileList.get(i));

        }
        return item.getId();
    }
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){
        //Entity
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        //DB 에서 데이터를 가지고 옵니다.
        //DTO
        List<ItemImgDto> itemImgDtoList =new ArrayList<>();

        for(ItemImg itemImg : itemImgList){
            //Entity -> DTO
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        //Item ->  ItemFormDto modelMapper
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList)
            throws  Exception{
        //상품 변경
        Item item = itemRepository.findById(itemFormDto.getId()).
                orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        item.setItemCategory(itemFormDto.getItemCategory()); // 카테고리 업데이트

        //상품 이미지 변경
        List<Long> itemImgIds = itemFormDto.getItemImgIds();
        for (int i =0; i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),itemImgFileList.get(i));
        }
        return item.getId();
    }

    @Transactional(readOnly = true) // 쿼리문 실행 읽기만 함
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto,pageable);
    }
    @Transactional(readOnly = true)
    public List<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto,int offset, int limit){
        return itemRepository.getMainItemPage(itemSearchDto, offset,limit);
    }
    @Transactional(readOnly = true)
    public List<MainItemDto> getCategoryItemPage(String category,int offset, int limit){


        return itemRepository.getCategoryItemPage(category, offset,limit);

    }
    public List<Item> searchItems(String itemDetail) {
        return itemRepository.findByItemDetailNative(itemDetail);
    }

}
