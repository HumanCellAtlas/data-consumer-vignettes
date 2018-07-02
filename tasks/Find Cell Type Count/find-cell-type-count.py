#!/usr/bin/env python3

import hca, json

def count_cell_types(cell_type):
    count = 0
    query = { "query": { "match": { "bundle.creator_uid": 0 } } }
    
    # Iterate through all search results containing the chosen cell type
    for result in hca.dss.DSSClient().post_search.iterate(es_query={}, replica='aws', output_format='raw'):
#        print(result)
#        bundle_uuid = result['bundle_fqid'][:36]
#        bundle_dict = hca.dss.DSSClient().get_bundle(uuid=bundle_uuid, replica='aws')
#        print()
        print(json.dumps(result, indent=4, sort_keys=True))
#        print()
#        print(json.dumps(hca.dss.DSSClient().get_file(uuid=bundle_dict.get('bundle').get('files')[0].get('uuid'), replica='aws'), indent=4, sort_keys=True))
        break
        
        # Add up the total number of cells in the sample
#        count += bundle.get('biomaterials')[0].get('content').get('total_estimated_cells')
    return count

def main():
    print(count_cell_types('spleen'))

main()
