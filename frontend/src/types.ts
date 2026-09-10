export type Photo = {
  id: number;
  sort_order: number;
  url: string;
  content_type?: string;
};

export type Scan = {
  id: number;
  created_at: string;
  brand?: string;
  category?: string;
  fabric?: string;
  sku?: string;
  list_price?: number | null;
  opening_offer?: number | null;
  target_min?: number | null;
  target_max?: number | null;
  max_price?: number | null;
  bargain_score?: number | null;
  reasons: string[];
  needs_retake_tag: boolean;
  retake_hint?: string | null;
  insufficient_evidence: boolean;
  mock_mode: boolean;
  bought?: boolean | null;
  final_price?: number | null;
  saved_amount?: number | null;
  photos: Photo[];
};

export type Status = {
  mock_mode: boolean;
  product: string;
};
