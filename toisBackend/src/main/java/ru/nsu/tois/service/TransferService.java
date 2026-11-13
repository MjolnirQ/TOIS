package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.CreateProductTransferWithItemsRequest;
import ru.nsu.tois.dto.request.ProductTransferItemRequest;
import ru.nsu.tois.dto.request.ProductTransferLineRequest;
import ru.nsu.tois.dto.request.ProductTransferRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.ProductTransferItemResponse;
import ru.nsu.tois.dto.response.ProductTransferResponse;
import ru.nsu.tois.dto.response.ProductTransferWithItemsResponse;
import ru.nsu.tois.entity.transfers.ProductTransfer;
import ru.nsu.tois.entity.transfers.ProductTransferItem;
import ru.nsu.tois.mapper.TransferMapper;
import ru.nsu.tois.repository.catalog.ProductRepository;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.repository.transfers.ProductTransferItemRepository;
import ru.nsu.tois.repository.transfers.ProductTransferRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {
    private final ProductTransferRepository productTransferRepository;
    private final ProductTransferItemRepository productTransferItemRepository;
    private final TradePointRepository tradePointRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final TransferMapper transferMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ProductTransferResponse> findAllProductTransfers(Integer page, Integer size) {
        return PaginationUtils.toResponse(productTransferRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(transferMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ProductTransferResponse findProductTransferById(Integer id) {
        return transferMapper.toDto(requireProductTransfer(id));
    }

    public ProductTransferResponse createProductTransfer(ProductTransferRequest request) {
        validateProductTransferRequest(request);
        return transferMapper.toDto(productTransferRepository.save(transferMapper.toEntity(request)));
    }

    public ProductTransferResponse updateProductTransfer(Integer id, ProductTransferRequest request) {
        ProductTransfer transfer = requireTransferForUpdate(id);
        if (!transfer.getSenderTradePoint().getId().equals(request.senderTradePointId())
                || !transfer.getReceiverTradePoint().getId().equals(request.receiverTradePointId())) {
            throw new IllegalStateException("The trade points of a transfer cannot be changed");
        }

        transfer.setTransferDate(request.transferDate());
        return transferMapper.toDto(transfer);
    }

    public void deleteProductTransfer(Integer id) {
        ProductTransfer productTransfer = requireTransferForUpdate(id);
        Integer senderId = productTransfer.getSenderTradePoint().getId();
        Integer receiverId = productTransfer.getReceiverTradePoint().getId();
        List<ProductTransferItem> items = productTransferItemRepository.findByProductTransfer_Id(id);
        for (ProductTransferItem item : items) {
            stockService.transfer(
                    receiverId,
                    senderId,
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
        productTransferItemRepository.deleteAll(items);
        productTransferRepository.delete(productTransfer);
    }

    public ProductTransferWithItemsResponse createProductTransferWithItems(CreateProductTransferWithItemsRequest request) {
        ProductTransferResponse productTransfer = createProductTransfer(new ProductTransferRequest(
                request.transferDate(),
                request.senderTradePointId(),
                request.receiverTradePointId()
        ));

        List<ProductTransferItemResponse> items = new ArrayList<>();
        for (ProductTransferLineRequest item : request.items()) {
            items.add(createProductTransferItem(new ProductTransferItemRequest(
                    productTransfer.id(),
                    item.productId(),
                    item.quantity()
            )));
        }

        return new ProductTransferWithItemsResponse(productTransfer, items);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductTransferItemResponse> findAllProductTransferItems(Integer page, Integer size) {
        return PaginationUtils.toResponse(productTransferItemRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(transferMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ProductTransferItemResponse findProductTransferItemById(Integer id) {
        return transferMapper.toDto(requireProductTransferItem(id));
    }

    public ProductTransferItemResponse createProductTransferItem(ProductTransferItemRequest request) {
        ProductTransfer transfer = requireTransferForUpdate(request.productTransferId());
        if (!productRepository.existsById(request.productId())) {
            throw notFound("Product", request.productId());
        }
        stockService.transfer(
                transfer.getSenderTradePoint().getId(),
                transfer.getReceiverTradePoint().getId(),
                request.productId(),
                request.quantity()
        );
        return transferMapper.toDto(productTransferItemRepository.save(transferMapper.toEntity(request)));
    }

    public ProductTransferItemResponse updateProductTransferItem(Integer id, ProductTransferItemRequest request) {
        ProductTransfer transfer = requireTransferForUpdate(request.productTransferId());
        ProductTransferItem existing = requireProductTransferItem(id);
        if (!existing.getProductTransfer().getId().equals(request.productTransferId())
                || !existing.getProduct().getId().equals(request.productId())) {
            throw new IllegalStateException("The transfer and product of a transfer item cannot be changed");
        }

        int senderId = transfer.getSenderTradePoint().getId();
        int receiverId = transfer.getReceiverTradePoint().getId();
        int difference = request.quantity() - existing.getQuantity();
        if (difference > 0) {
            stockService.transfer(senderId, receiverId, request.productId(), difference);
        } else if (difference < 0) {
            stockService.transfer(receiverId, senderId, request.productId(), -difference);
        }

        existing.setQuantity(request.quantity());
        return transferMapper.toDto(existing);
    }

    public void deleteProductTransferItem(Integer id) {
        Integer transferId = productTransferItemRepository.findTransferIdByItemId(id)
                .orElseThrow(() -> notFound("ProductTransferItem", id));
        requireTransferForUpdate(transferId);
        ProductTransferItem item = requireProductTransferItem(id);
        stockService.transfer(
                item.getProductTransfer().getReceiverTradePoint().getId(),
                item.getProductTransfer().getSenderTradePoint().getId(),
                item.getProduct().getId(),
                item.getQuantity()
        );
        productTransferItemRepository.delete(item);
    }

    private void validateProductTransferRequest(ProductTransferRequest request) {
        tradePointRepository.findById(request.senderTradePointId())
                .orElseThrow(() -> notFound("TradePoint", request.senderTradePointId()));
        tradePointRepository.findById(request.receiverTradePointId())
                .orElseThrow(() -> notFound("TradePoint", request.receiverTradePointId()));
        if (request.senderTradePointId().equals(request.receiverTradePointId())) {
            throw new IllegalArgumentException("Sender and receiver trade points must be different");
        }
    }

    private ProductTransfer requireTransferForUpdate(Integer id) {
        return productTransferRepository.findLockedById(id)
                .orElseThrow(() -> notFound("ProductTransfer", id));
    }

    private ProductTransfer requireProductTransfer(Integer id) {
        return productTransferRepository.findById(id).orElseThrow(() -> notFound("ProductTransfer", id));
    }

    private ProductTransferItem requireProductTransferItem(Integer id) {
        return productTransferItemRepository.findById(id).orElseThrow(() -> notFound("ProductTransferItem", id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}
