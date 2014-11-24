imagelibs
=========

android DiskLruCache and memoryCache for image

1:
  private ImageResizer mImageWorker;
  
  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initImageCache();
      
    }
2:  
  private void initImageCache(){
  
        //缓存目录
        ImageCacheParams cacheParams = new ImageCacheParams("http");
        
        //缓存图片大小
        mImageWorker = new ImageFetcher(getActivity(), 200);
        
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),cacheParams));
        
        //LOADING图片
        mImageWorker.setLoadingImage(R.drawable.LOADING);
    }
    
3：
mImageWorker.loadImage(“YOUR IMG URL”,your ImageView WIDGET );
